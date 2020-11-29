package com.kreait.bots.agile.domain.slack.standupDefinition.delete.help

import com.kreait.bots.agile.UnitTest
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.group.chat.ErrorPostEphemeralResponse
import com.kreait.slack.api.contract.jackson.group.chat.PostEphemeralRequest
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulPostEphemeralResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class SlackDeletionHelpMessageServiceTests {

    @DisplayName("Test delete help-message service")
    @Test
    fun testDeletionMessageService() {
        val slackClient = MockSlackClient()
        slackClient.chat().postEphemeral("sampleToken").successResponse = SuccessfulPostEphemeralResponse.sample()


        val deletionHelpMessageService = SlackDeletionHelpMessageService(slackClient)
        val command = SlackCommand.sample()

        deletionHelpMessageService.sendHelpMessage(command, "")

        val expectedParam = PostEphemeralRequest(text = "Hoot  hoot, looks like you need help for the stand-up deletion, " +
                "just run the command, select the stand-up which you want to delete and hit submit.  If you need further help, send us a message at help@olaph.io", channel = command.channelId, user = command.userId)

        Assertions.assertEquals(slackClient.chat().postEphemeral("").params(), expectedParam)
    }

    @DisplayName("Test failure delete help-message service")
    @Test
    fun testFailureDeletionMessageService() {
        val slackClient = MockSlackClient()

        slackClient.chat().postEphemeral("sampleToken").failureResponse = ErrorPostEphemeralResponse.sample()

        val deletionHelpMessageService = SlackDeletionHelpMessageService(slackClient)
        val command = SlackCommand.sample()

        deletionHelpMessageService.sendHelpMessage(command, "")

        val expectedParam = PostEphemeralRequest(text = "Hoot  hoot, looks like you need help for the stand-up deletion, " +
                "just run the command, select the stand-up which you want to delete and hit submit.  If you need further help, send us a message at help@olaph.io", channel = command.channelId, user = command.userId)
        Assertions.assertEquals(slackClient.chat().postEphemeral("").params(), expectedParam)
    }
}
