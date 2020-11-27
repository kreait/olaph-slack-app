package com.kreait.bots.agile.domain.slack.standupDefinition.create.help

import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.group.chat.ErrorPostEphemeralResponse
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulPostEphemeralResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SlackCreationHelpMessageServiceTest {

    @DisplayName("Test successful help message sender")
    @Test
    fun testHelpSender() {
        val slackClient = MockSlackClient()
        slackClient.chat().postEphemeral("").successResponse = SuccessfulPostEphemeralResponse.sample()
        val slackCreationHelpMessageService = SlackCreationHelpMessageService(slackClient)
        slackCreationHelpMessageService.sendHelpMessage(SlackCommand.sample(), "")
    }

    @DisplayName("Test failure help message sender")
    @Test
    fun testHelpSenderFailure() {
        val slackClient = MockSlackClient()
        slackClient.chat().postEphemeral("").failureResponse = ErrorPostEphemeralResponse.sample()
        val slackCreationHelpMessageService = SlackCreationHelpMessageService(slackClient)
        slackCreationHelpMessageService.sendHelpMessage(SlackCommand.sample(), "")
    }
}