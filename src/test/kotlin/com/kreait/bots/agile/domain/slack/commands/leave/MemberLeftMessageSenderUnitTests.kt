package com.kreait.bots.agile.domain.slack.commands.leave

import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.slack.standupDefinition.leave.MemberLeftMessageSender
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.group.channels.SuccessfulChannelInfoResponse
import com.kreait.slack.api.contract.jackson.group.channels.sample
import com.kreait.slack.api.contract.jackson.group.chat.ErrorPostMessageResponse
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulPostMessageResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Locale

class MemberLeftMessageSenderUnitTests {

    private val userId = "SampleUserID"
    private val teamId = "SampleTeamID"
    private val channelId = "SampleChannelID"

    @DisplayName("Successful sendCancelOpenStandupMessage")
    @Test
    fun successfulSendCancelOpenStandupMessage() {

        val mockSlackClient = MockSlackClient()
        val service = MemberLeftMessageSender(
                userChannelIdService = mock { on { fetchChannelIdByUserId(any(), any()) } doReturn channelId },
                message = mock { on { getMessage(any<String>(), any<Locale>()) } doReturn "something" },
                slackClient = mockSlackClient)

        service.sendCancelOpenStandupMessage(userId, StandupDefinition.sample(), "")

        val expected = PostMessageRequest(
                channel = channelId,
                text = "something"
        )
        Assertions.assertEquals(expected, mockSlackClient.chat().postMessage("").params())
    }

    @DisplayName("Successful sendChannelLeftMessage")
    @Test
    fun successfulSendChannelLeftMessage() {

        val mockSlackClient = MockSlackClient()

        mockSlackClient.chat().postMessage("").successResponse = SuccessfulPostMessageResponse.sample()

        mockSlackClient.channel().info("").successResponse = SuccessfulChannelInfoResponse.sample()

        val service = MemberLeftMessageSender(
                userChannelIdService = mock { on { fetchChannelIdByUserId(any(), any()) } doReturn channelId },
                message = mock { on { getMessage(any<String>(), any<Locale>()) } doReturn "something" },
                slackClient = mockSlackClient)

        service.sendChannelLeftMessage(userId = userId, standups = listOf(StandupDefinition.sample()), channelId = channelId, accessToken = "")

        val expected = PostMessageRequest(
                channel = channelId,
                text = "something",
                attachments = listOf(Attachment(
                        text = "Workspace",
                        fallback = "Workspace"
                ))
        )

        Assertions.assertEquals(expected, mockSlackClient.chat().postMessage("").params())
    }

    @DisplayName("Failure sendCancelOpenStandupMessage")
    @Test
    fun failureSendCancelOpenStandupMessage() {
        val mockSlackClient = MockSlackClient()

        mockSlackClient.chat().postMessage("").failureResponse = ErrorPostMessageResponse.sample()
        val service = MemberLeftMessageSender(
                userChannelIdService = mock { on { fetchChannelIdByUserId(any(), any()) } doReturn channelId },
                message = mock { on { getMessage(any<String>(), any<Locale>()) } doReturn "something" },
                slackClient = mockSlackClient)

        service.sendCancelOpenStandupMessage(userId, StandupDefinition.sample(), "")

        val expected = PostMessageRequest(
                channel = channelId,
                text = "something_wrong"
        )
        Assertions.assertNotEquals(expected, mockSlackClient.chat().postMessage("").params())
    }

    @DisplayName("Failure sendChannelLeftMessage")
    @Test
    fun failureSendChannelLeftMessage() {
        val mockSlackClient = MockSlackClient()

        mockSlackClient.channel().info("").successResponse = SuccessfulChannelInfoResponse.sample()

        val service = MemberLeftMessageSender(
                userChannelIdService = mock { on { fetchChannelIdByUserId(any(), any()) } doReturn channelId },
                message = mock { on { getMessage(any<String>(), any<Locale>()) } doReturn "something" },
                slackClient = mockSlackClient)

        service.sendChannelLeftMessage(userId = userId, standups = listOf(StandupDefinition.sample()), channelId = channelId, accessToken = "")

        val expected = PostMessageRequest(
                channel = channelId,
                text = "wrong_string",
                attachments = listOf(Attachment(
                        text = "wrong_string",
                        fallback = "wrong_string"
                ))
        )

        Assertions.assertNotEquals(expected, mockSlackClient.chat().postMessage("").params())
    }
}
