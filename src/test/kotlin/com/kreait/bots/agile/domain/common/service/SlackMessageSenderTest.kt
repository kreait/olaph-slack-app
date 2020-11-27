package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.domain.response.RandomResponseProvider
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.group.chat.ErrorPostEphemeralResponse
import com.kreait.slack.api.contract.jackson.group.chat.ErrorPostMessageResponse
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulPostMessageResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SlackMessageSenderTest {
    private lateinit var userChannelIdService: UserChannelIdService
    private lateinit var randomResponseProvider: RandomResponseProvider
    private lateinit var messageContext: MessageContext
    private lateinit var slackClient: MockSlackClient
    private lateinit var messageSender: SlackMessageSender


    @BeforeEach
    fun setup() {
        userChannelIdService = mock {
            on { fetchChannelIdByUserId(any(), any()) } doReturn "sampleChannelId"
        }
        randomResponseProvider = mock { on { getRandomizedResponse(any(), any()) } doReturn "sampleResponse" }
        messageContext = MessageContext(channelName = "test",
                teamId = "sampleTeam",
                channelId = "sampleChannelId",
                userId = "sampleUser")
        slackClient = MockSlackClient()
        messageSender = SlackMessageSender(slackClient, randomResponseProvider, userChannelIdService)
    }

    @DisplayName("Test Successful Ephemeral")
    @Test
    fun testSuccessEphemeralMessageSender() {
        messageSender.sendEphemeralMessage(
                ResponseType.NO_STANDUPS_FOUND,
                messageContext = messageContext.copy(channelName = "directmessage"),
                token = "")
        verify(randomResponseProvider, times(1)).getRandomizedResponse(eq(ResponseType.NO_STANDUPS_FOUND), any())
    }

    @DisplayName("Test Failure Ephemeral")
    @Test
    fun testFailureEphemeralMessageSender() {
        slackClient.chat().postEphemeral("sampleToken").failureResponse = ErrorPostEphemeralResponse.sample()
        messageSender.sendEphemeralMessage(ResponseType.NO_STANDUPS_FOUND,
                messageContext = messageContext, token = "")
        verify(randomResponseProvider, times(1)).getRandomizedResponse(ResponseType.NO_STANDUPS_FOUND, messageContext)
    }

    @DisplayName("Test Successful SendMessage")
    @Test
    fun testSuccessMessageSender() {
        slackClient.chat().postMessage("sampleToken").successResponse = SuccessfulPostMessageResponse.sample()
        val expectedParam =
                PostMessageRequest(
                        text = randomResponseProvider.getRandomizedResponse(type = ResponseType.NO_STANDUPS_FOUND, messageContext = messageContext),
                        channel = messageContext.channelId!!,
                        attachments = listOf()
                )

        messageSender.sendMessage(ResponseType.NO_STANDUPS_FOUND, messageContext = messageContext.copy(channelName = "sample", channelId = null), token = "")
        verify(randomResponseProvider, times(2)).getRandomizedResponse(eq(ResponseType.NO_STANDUPS_FOUND), any())
        Assertions.assertEquals((slackClient.chat().postMessage("")).params(), expectedParam)
    }

    @DisplayName("Test Failure SendMessage")
    @Test
    fun testFailureMessageSender() {
        slackClient.chat().postMessage("sampleToken").failureResponse = ErrorPostMessageResponse.sample()
        messageSender.sendMessage(ResponseType.NO_STANDUPS_FOUND, messageContext = messageContext, token = "")
        verify(randomResponseProvider, times(1)).getRandomizedResponse(ResponseType.NO_STANDUPS_FOUND, messageContext)
    }
}
