package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.help

import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.group.chat.PostEphemeralRequest
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulPostEphemeralResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SlackLeaveHelpMessageTest {

    @DisplayName("Test Help message test")
    @Test
    fun testLeaveHelpMessage() {
        val slackClient = MockSlackClient()
        slackClient.chat().postEphemeral("").successResponse = SuccessfulPostEphemeralResponse.sample()
        val slackLeaveHelpMessageService = SlackLeaveHelpMessageService(slackClient)
        slackLeaveHelpMessageService.sendHelpMessage(SlackCommand.sample(), "")
        val expectedParam = PostEphemeralRequest(text = "Hoot hoot, looks like you need help leaving a stand-up," +
                "just run the command, select the standup you want to leave and hit submit. If you need further help" +
                ", send us a message at help@olaph.io",
                channel = "",
                user = "")
        Assertions.assertEquals(slackClient.chat().postEphemeral("").params(), expectedParam)

    }
}