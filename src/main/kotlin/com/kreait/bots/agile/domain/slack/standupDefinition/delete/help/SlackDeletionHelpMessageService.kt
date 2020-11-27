package com.kreait.bots.agile.domain.slack.standupDefinition.delete.help

import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.group.chat.PostEphemeralRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SlackDeletionHelpMessageService @Autowired constructor(private val slackClient: SlackClient) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SlackDeletionHelpMessageService::class.java)
    }

    fun sendHelpMessage(slackCommand: SlackCommand, accessToken: String) {
        this.slackClient.chat()
                .postEphemeral(accessToken)
                .with(PostEphemeralRequest(text = "Hoot  hoot, looks like you need help for the stand-up deletion, " +
                        "just run the command, select the stand-up which you want to delete and hit submit.  If you need further help, send us a message at help@olaph.io", channel = slackCommand.channelId, user = slackCommand.userId))
                .onSuccess {
                    if (LOG.isDebugEnabled) {
                        LOG.debug("Successfully sent help-message")
                    }
                }
                .onFailure {
                    LOG.error("Failed to send help-message")
                }
                .invoke()
    }
}
