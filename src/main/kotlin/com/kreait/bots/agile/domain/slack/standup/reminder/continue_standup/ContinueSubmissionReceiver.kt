package com.kreait.bots.agile.domain.slack.standup.reminder.continue_standup

import com.kreait.bots.agile.domain.slack.standup.reminder.ReminderService
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.group.chat.ChatDeleteRequest
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class ContinueSubmissionReceiver @Autowired constructor(private val slackClient: SlackClient) : InteractiveComponentReceiver<InteractiveMessage> {

    companion object {
        private val LOG = LoggerFactory.getLogger(ContinueSubmissionReceiver::class.java)
    }

    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean {
        return interactiveComponentResponse.callbackId == ReminderService.REMINDER_CALLBACK &&
                interactiveComponentResponse.actions!![0].name == ReminderService.CONTINUE_BUTTON_NAME
    }

    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        interactiveComponentResponse.team.let {
            this.slackClient.chat().delete(team.bot.accessToken)
                    .with(ChatDeleteRequest(channel = interactiveComponentResponse.channel.id,
                            //TODO check before introducing blocks
                            timestamp = interactiveComponentResponse.timestamp!!))
                    .onSuccess {
                        when {
                            LOG.isDebugEnabled -> LOG.debug("Successfully deleted reminder")
                        }
                    }.onFailure {
                        LOG.error("Error while deleting reminder")
                    }.invoke()

        }
    }
}
