package com.kreait.bots.agile.domain.slack.standupDefinition.leave

//TODO should this package be located in the slack package or here in standupDefinition?


import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.SlackEventTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import com.kreait.slack.api.contract.jackson.event.sample
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.HttpHeaders

class MemberLeftEventReceiverTest : SlackEventTest {

    @Test
    @DisplayName("Test Supports leave Event")
    override fun supportsEvent() {
        val service = mock<MemberLeftMessageSender> {}
        val receiver = MemberLeftEventReceiver(mock { }, mock { }, service)
        Assertions.assertTrue(
                receiver.supportsEvent(SlackEvent.sample().copy(event = mapOf(
                        Pair("type", "member_left_channel"),
                        Pair("user", "testUser"),
                        Pair("channel", "testChannel"),
                        Pair("team", "testTeam"))))
        )
        Assertions.assertFalse(
                receiver.supportsEvent(SlackEvent.sample().copy(event = mapOf(
                        Pair("type", "member_left_channel"),
                        Pair("bot_user", "bot"),
                        Pair("channel", "testChannel"),
                        Pair("team", "testTeam")))))
        Assertions.assertFalse(
                receiver.supportsEvent(SlackEvent.sample().copy(event = mapOf(
                        Pair("type", "member_joined_channel"),
                        Pair("user", "testUser"),
                        Pair("channel", "testChannel"),
                        Pair("team", "testTeam")))))
    }

    @Test
    @DisplayName("Test receive Event")
    override fun onReceiveEvent() {
        val messageSender = mock<MemberLeftMessageSender> {}


        val sampleStandupDef = StandupDefinition.sample().copy(id = "SampleDefId")
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on { find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } doReturn listOf(sampleStandupDef)
        }


        val standupRepository = mock<StandupRepository> {
            on { find(any(), eq("SampleDefId"), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } doReturn listOf(Standup.sample().copy(status = Standup.Status.OPEN))
        }

        val receiver = MemberLeftEventReceiver(standupDefinitionRepository, standupRepository, messageSender)
        val event = SlackEvent.sample().copy(event = mapOf(
                Pair("type", "member_left_channel"),
                Pair("user", "testUser"),
                Pair("channel", "testChannel"),
                Pair("team", "testTeam")))
        receiver.onReceiveEvent(slackEvent = event, headers = HttpHeaders.EMPTY, team = com.kreait.slack.broker.store.team.Team("testTeam", "",
                com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                com.kreait.slack.broker.store.team.Team.Bot("", "")))

        verify(messageSender, times(0)).sendCancelOpenStandupMessage(event.event["user"].toString(),
                StandupDefinition.sample(), "")

        verify(standupRepository, times(1)).update(any(), eq("SampleDefId"), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), eq(Update.update(Standup.STATUS, Standup.Status.CLOSED)))

        verify(messageSender, times(1)).sendChannelLeftMessage(event.event["user"].toString(),
                listOf(sampleStandupDef),
                event.event["channel"].toString(),
                "")


    }
}
