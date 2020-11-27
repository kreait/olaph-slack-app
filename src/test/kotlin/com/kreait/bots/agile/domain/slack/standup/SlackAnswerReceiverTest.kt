package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.core.standup.answer.StandupAnswerReceiver
import com.kreait.bots.agile.domain.slack.SlackEventTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import com.kreait.slack.api.contract.jackson.event.sample
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class SlackAnswerReceiverTest : SlackEventTest {
    @DisplayName("Test Supports event")
    @Test
    override fun supportsEvent() {
        val receiver = SlackAnswerReceiver(mock())

        Assertions.assertTrue(receiver.supportsEvent(SlackEvent.sample().copy(
                event = mapOf(
                        Pair("user", "sampleUser"),
                        Pair("client_msg_id", "sample"),
                        Pair("type", "message"),
                        Pair("channel_type", "im")
                ))))
        Assertions.assertFalse(receiver.supportsEvent(SlackEvent.sample().copy(
                event = mapOf(
                        Pair("client_msg_id", "sample"),
                        Pair("type", "member_left_channel"),
                        Pair("channel_type", "channel")
                ))))
    }

    @DisplayName("Test Answer Receiver")
    @Test
    override fun onReceiveEvent() {
        val standupAnswerReceiver = mock<StandupAnswerReceiver> {}
        val receiver = SlackAnswerReceiver(standupAnswerReceiver)
        val event = SlackEvent.sample().copy(
                event = mapOf(
                        Pair("text", "sampleText"),
                        Pair("user", "sampleUser"),
                        Pair("client_msg_id", "sample"),
                        Pair("type", "message"),
                        Pair("channel_type", "im")
                ))
        receiver.onReceiveEvent(event, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        verify(standupAnswerReceiver, times(1)).handleAnswer("sampleUser",
                "sampleText", event.eventId, event.eventTime, event.teamId)
    }
}