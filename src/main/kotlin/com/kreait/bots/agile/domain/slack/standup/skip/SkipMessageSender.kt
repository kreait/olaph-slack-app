package com.kreait.bots.agile.domain.slack.standup.skip

import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.SlackCommand
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SkipMessageSender @Autowired constructor(private val messageSender: SlackMessageSender) {
    companion object {
        private val Log = LoggerFactory.getLogger(SkipMessageSender::class.java)
    }

    /**
     * sends a message to the user, informing that the standup was skipped
     */
    fun sendSuccesfulSkipMessage(slackCommand: SlackCommand, standupName: String, accessToken: String) {
        messageSender.sendEphemeralMessage(ResponseType.SUCCESFUL_SKIPPED, messageContext = MessageContext.of(slackCommand).copy(currentStandup = standupName), token = accessToken)
    }

    /**
     * sends a message to the user, informing that there are no open standups
     */
    fun sendNoStandupsFoundMessage(slackCommand: SlackCommand, accessToken: String) {
        messageSender.sendEphemeralMessage(ResponseType.NO_STANDUPS_FOUND, messageContext = MessageContext.of(slackCommand), token = accessToken)
    }
}
