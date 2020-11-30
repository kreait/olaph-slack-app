package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.UserChannelIdService
import com.kreait.bots.agile.domain.response.RandomResponseProvider
import com.kreait.bots.agile.domain.response.ResponseType
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.api.contract.jackson.group.chat.ErrorPostMessageResponse
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulPostMessageResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class SlackOpeningMessageSenderTest {

    private lateinit var randomResponseProvider: RandomResponseProvider
    private lateinit var userChannelIdService: UserChannelIdService
    private lateinit var slackClient: MockSlackClient
    private val standup = Standup.sample()
    @BeforeEach
    fun setup() {
        slackClient = MockSlackClient()
        userChannelIdService = mock {
            on { fetchChannelIdByUserId(any(), any()) } doReturn "sampleChannel"
        }
        randomResponseProvider = mock {
            on { getRandomizedResponse(any(), any()) } doReturn "sampleResponse"
        }
    }

    @DisplayName("Test successful Opening message Sender")
    @Test
    fun testOpeningSender() {
        slackClient.chat().postMessage("sampleToken").successResponse = SuccessfulPostMessageResponse.sample()
        val slackOpeningMessageSender = SlackOpeningMessageSender(slackClient, userChannelIdService, randomResponseProvider, mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })

        val expected = PostMessageRequest(
                channel = "sampleChannel",
                text = randomResponseProvider.getRandomizedResponse(ResponseType.OPENING,
                        MessageContext(date = standup.date, currentStandup = standup.name, teamId = standup.teamId, userId = standup.userId)))
        Assertions.assertTrue(slackOpeningMessageSender.sendOpeningMessage(standup).success)
        Assertions.assertEquals(slackClient.chat().postMessage("").params(), expected)
    }

    @DisplayName("Test failure Opening message Sender")
    @Test
    fun testFailureOpeningSender() {
        slackClient.chat().postMessage("sampleToken").failureResponse = ErrorPostMessageResponse.sample()

        val slackOpeningMessageSender = SlackOpeningMessageSender(slackClient, userChannelIdService, randomResponseProvider, mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })

        Assertions.assertFalse(slackOpeningMessageSender.sendOpeningMessage(standup).success)
    }
}