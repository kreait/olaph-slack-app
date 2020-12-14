package com.kreait.bots.agile.domain.slack.standup.trigger

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.group.chat.PostEphemeralRequest
import com.kreait.slack.api.contract.jackson.group.dialog.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TriggerStandupDialog @Autowired constructor(
    private val slackClient: SlackClient,
    private val standupRepository: StandupRepository,
    private val slackMessageSender: SlackMessageSender
) {
    companion object {
        private val Log = LoggerFactory.getLogger(SlackTriggerCommandReceiver::class.java)
        const val CALLBACK_ID = "standupTrigger"
        const val STANDUP_SELECTION = "standupSelection"
    }

    /**
     * opens the Standup-selection dialog
     */
    fun openDialog(slackCommand: SlackCommand, accessToken: String) {
        val options = createOptions(slackCommand.userId, slackCommand.channelId, accessToken)
        if (options.isNotEmpty()) {
            this.slackClient.dialog()
                .open(accessToken)
                .with(
                    OpenDialogRequest(
                        trigger_id = slackCommand.triggerId,
                        dialog = Dialog(
                            callback_id = CALLBACK_ID,
                            title = "Trigger a Stand-up",
                            elements = listOf(
                                SelectElement(
                                    label = "Select the stand-up",
                                    name = STANDUP_SELECTION,
                                    options = options,
                                    type = Type.SELECT
                                )
                            )
                        )
                    )
                ).onSuccess {
                    when {
                        Log.isDebugEnabled -> {
                            Log.debug("Successfully opened Dialog $it")
                        }
                    }
                }
                .onFailure {
                    Log.error("Failure while opening Dialog $it")
                }.invoke()
        } else {
            slackMessageSender.sendEphemeralMessage(
                ResponseType.NO_STANDUPS_FOUND,
                messageContext = MessageContext.of(slackCommand),
                token = accessToken
            )
        }
    }


    /**
     * creates a list of Options containing the standups where the user is a member of
     */
    private fun createOptions(userId: String, channel: String, accessToken: String): List<Options> {
        return if (this.standupRepository.exists(
                withUserIds = setOf(userId),
                withStatus = setOf(Standup.Status.OPEN),
                isOnDate = LocalDate.now()
            )
        ) {
            sendErrorMessage(userId, channel, accessToken)
            listOf()
        } else {
            this.standupRepository.find(withUserIds = setOf(userId), withStatus = setOf(Standup.Status.CREATED))
                .map {
                    Options(label = it.name, value = it.standupDefinitionId)
                }
        }
    }

    /**
     * sends an error message to the user if he already has an open standup
     */
    private fun sendErrorMessage(userId: String, channel: String, accessToken: String) {
        //TODO Randomize message
        this.slackClient.chat().postEphemeral(accessToken)
            .with(
                PostEphemeralRequest(
                    text = "Sorry, I can't start this standup for you, yet. You still have an active standup going on.",
                    channel = channel,
                    user = userId
                )
            )
            .onSuccess {
                when {
                    Log.isDebugEnabled ->
                        Log.debug("successfully opened Dialog $it")
                }
            }
            .onFailure {
                Log.error("Failure while opening Dialog $it")
            }.invoke()
    }

}
