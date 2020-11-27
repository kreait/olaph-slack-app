package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.domain.response.ResponseType
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.broker.store.team.Team
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class RandomAnswerServiceTest {

    private lateinit var randomAnswerService: RandomAnswerService
    private lateinit var messageSender: SlackMessageSender

    @BeforeEach
    fun setup() {
        messageSender = mock {}
        randomAnswerService = RandomAnswerService(messageSender, mock {
            on { findById(any()) } doReturn Team("", "",
                    Team.IncomingWebhook("", "", "", ""),
                    Team.Bot("", ""))
        })
    }

    @DisplayName("Test Hoot Hoot Answer")
    @Test
    fun testHootAnswer() {
        randomAnswerService.handleMessage("sampleUser", "hoot hoot", "sampleTeam")
        verify(messageSender, times(1)).sendMessage(eq(ResponseType.HOOT_RESPONSES), any(), any(), eq(""))
        verify(messageSender, times(0)).sendMessage(eq(ResponseType.UNKNOWN_MESSAGE_RESPONSE), any(), any(), eq(""))
    }

    @DisplayName("Test Unknown Answer")
    @Test
    fun testUnknownAnswer() {
        randomAnswerService.handleMessage("sampleUser", "sampleMessage", "sampleTeam")
        verify(messageSender, times(0)).sendMessage(eq(ResponseType.HOOT_RESPONSES), any(), any(), eq(""))
        verify(messageSender, times(1)).sendMessage(eq(ResponseType.UNKNOWN_MESSAGE_RESPONSE), any(), any(), eq(""))
    }

}