package com.kreait.bots.agile.domain.slack.standupDefinition.join

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.slack.SlackEventTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import com.kreait.slack.api.contract.jackson.event.sample
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

@UnitTest
class MemberJoinedChannelReceiverTest : SlackEventTest {

    @DisplayName("Test Member joined supports event")
    @Test
    override fun supportsEvent() {
        val memberJoinedChannelReceiver = MemberJoinedChannelReceiver(mock(), mock())

        Assertions.assertTrue(memberJoinedChannelReceiver.supportsEvent(SlackEvent.sample().copy(event = mapOf(
                Pair("type", "member_joined_channel"),
                Pair("user", "sampleUser"),
                Pair("channel", "sampleChannel"),
                Pair("team", "sampleTeam")
        ))))
        Assertions.assertFalse(memberJoinedChannelReceiver.supportsEvent(SlackEvent.sample().copy(event = mapOf(
                Pair("type", "member_joined_channel"),
                Pair("wrongKey", "sampleUser"),
                Pair("channel", "sampleChannel"),
                Pair("team", "sampleTeam")
        ))))
    }

    @DisplayName("Test Member joined receiver")
    @Test
    override fun onReceiveEvent() {
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                find(withTeamId = "sampleTeam")
            } doReturn listOf(StandupDefinition.sample().copy(broadcastChannelId = "sampleChannel"))
            on {
                save(any<StandupDefinition>())
            } doReturn StandupDefinition.sample().copy(broadcastChannelId = "sampleChannel")
        }
        val messageSender = mock<SlackMessageSender>()
        val event = SlackEvent.sample().copy(event = mapOf(
                Pair("type", "member_joined_channel"),
                Pair("user", "sampleUser"),
                Pair("channel", "sampleChannel"),
                Pair("team", "sampleTeam")
        ))
        val memberJoinedChannelReceiver = MemberJoinedChannelReceiver(standupDefinitionRepository, messageSender)
        memberJoinedChannelReceiver.onReceiveEvent(event, HttpHeaders.EMPTY, Team("", "",
                Team.Bot("", "")))
    }
}
