package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.common.service.UserChannelIdService
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
class SlackQuestionSenderTest {

    private lateinit var userChannelIdService: UserChannelIdService
    private lateinit var slackClient: MockSlackClient

    @BeforeEach
    fun setup() {
        slackClient = MockSlackClient()
        userChannelIdService = mock {
            on { fetchChannelIdByUserId(any(), any()) } doReturn "sampleChannel"
        }
    }

    @DisplayName("Test successful Question Sender")
    @Test
    fun testSuccessQuestionSender() {
        slackClient.chat().postMessage("sampleToken").successResponse = SuccessfulPostMessageResponse.sample()
        val slackQuestionSender = SlackQuestionSender(slackClient, userChannelIdService, mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })
        val expected = PostMessageRequest("Test Question", channel = "sampleChannel")
        slackQuestionSender.sendQuestion("sampleUser", "Test Question", "sampleTeam")
        Assertions.assertEquals(slackClient.chat().postMessage("").params(), expected)
    }

    @DisplayName("Test failure Question Sender")
    @Test
    fun testFailureQuestionSender() {
        slackClient.chat().postMessage("sampleToken").failureResponse = ErrorPostMessageResponse.sample()
        val slackQuestionSender = SlackQuestionSender(slackClient, userChannelIdService, mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })
        slackQuestionSender.sendQuestion("sampleUser", "Test Question", "sampleTeam")
    }
}