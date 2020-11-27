package com.kreait.bots.agile.domain.slack.standup.reminder.skip

import com.kreait.bots.agile.core.standup.open.StandupOpeningService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.standup.reminder.ReminderService
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.group.chat.ChatDeleteRequest
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class SkipSubmissionReceiver @Autowired constructor(private val standupRepository: StandupRepository,
                                                    private val standupOpeningService: StandupOpeningService,
                                                    private val slackClient: SlackClient) : InteractiveComponentReceiver<InteractiveMessage> {
    companion object {
        val Log: Logger = LoggerFactory.getLogger(SkipSubmissionReceiver::class.java)
    }

    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean {
        return interactiveComponentResponse.callbackId == ReminderService.REMINDER_CALLBACK &&
                interactiveComponentResponse.actions!![0].name == ReminderService.SKIP_BUTTON_NAME

    }

    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        standupRepository.update(withUserIds = setOf(interactiveComponentResponse.user.id),
                withStatus = setOf(Standup.Status.OPEN),
                update = Update.update(Standup.STATUS, Standup.Status.CANCELLED))

        interactiveComponentResponse.team.let {
            this.slackClient.chat().delete(team.bot.accessToken)
                    .with(ChatDeleteRequest(channel = interactiveComponentResponse.channel.id,
                            //TODO check before introducing blocks
                            timestamp = interactiveComponentResponse.timestamp!!))
                    .onSuccess {
                        when {
                            Log.isDebugEnabled -> Log.debug("Successfully deleted reminder")
                        }
                    }.onFailure {
                        Log.error("Error while deleting reminder")
                    }.invoke()


            standupOpeningService.openStandup(interactiveComponentResponse.user.id, it.id)

        }
    }
}
