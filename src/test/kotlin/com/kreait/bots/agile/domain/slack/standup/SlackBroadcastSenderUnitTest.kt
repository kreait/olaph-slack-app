package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.common.service.UserChannelIdService
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.common.types.Member
import com.kreait.slack.api.contract.jackson.common.types.sample
import com.kreait.slack.api.contract.jackson.group.chat.ErrorPostMessageResponse
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulPostMessageResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.broker.store.team.Team
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.support.MessageSourceAccessor
import java.time.LocalDate
import java.util.Locale

@UnitTest
class SlackBroadcastSenderUnitTest() {
    private lateinit var message: MessageSourceAccessor
    private lateinit var user: Member
    private lateinit var slackClient: MockSlackClient
    private lateinit var userChannelIdService: UserChannelIdService
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        user = Member.sample()
        slackClient = MockSlackClient()
        slackClient.chat().postMessage("sampleToken").successResponse =
                SuccessfulPostMessageResponse.sample()
        userChannelIdService = mock {
            on { fetchChannelIdByUserId(any(), any()) } doReturn "anyUser"
        }
        userService = mock { on { user(any(), any()) } doReturn user }
        message = mock { on { getMessage(any<String>(), any<Locale>()) } doReturn "sampleMessage" }
    }

    private fun setFailureResponse() {
        slackClient.chat().postMessage("sampleToken").failureResponse = ErrorPostMessageResponse(false, "")
        slackClient.chat().postMessage("sampleToken").successResponse = null
    }

    @Test
    fun testBroadcastSender() {
        val broadcastSender = SlackBroadcastSender(slackClient, userChannelIdService, userService,
                message, mock(), mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })
        broadcastSender.sendBroadcast("", "", "", LocalDate.now(),
                listOf("question"), listOf("answer"), "sampleTeam")
    }

    @Test
    fun testBroadcastConfirmationSender() {

        val broadcastSender = SlackBroadcastSender(slackClient, userChannelIdService, userService,
                message, mock {
            on { getRandomizedResponse(ResponseType.BROADCAST_CONFIRMATION) } doReturn ""
        }, mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })

        Assertions.assertTrue(broadcastSender.sendBroadcastConfirmation("", ""))
    }

    @Test
    fun testFailureBroadcastConfirmation() {
        setFailureResponse()
        val broadcastSender = SlackBroadcastSender(slackClient, userChannelIdService, userService,
                message, mock {
            on { getRandomizedResponse(ResponseType.BROADCAST_CONFIRMATION) } doReturn ""
        }, mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })
        Assertions.assertFalse(broadcastSender.sendBroadcastConfirmation("", ""))
    }

    @Test
    fun testNullChannelBroadcastConfirmation() {
        userChannelIdService = mock {
            on { fetchChannelIdByUserId(any(), any()) } doReturn null
        }
        val broadcastSender = SlackBroadcastSender(slackClient, userChannelIdService, userService,
                message, mock(), mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })
        Assertions.assertFalse(broadcastSender.sendBroadcastConfirmation("", ""))
    }

    @Test
    fun testFailureBroadcast() {
        setFailureResponse()

        val broadcastSender = SlackBroadcastSender(slackClient, userChannelIdService, userService,
                message, mock(), mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })
        broadcastSender.sendBroadcast("", "", "", LocalDate.now(),
                listOf("question"), listOf("answer"), "sampleTeam")
    }
}
