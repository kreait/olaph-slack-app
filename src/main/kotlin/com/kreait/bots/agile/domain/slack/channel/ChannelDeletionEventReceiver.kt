package com.kreait.bots.agile.domain.slack.channel

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.slack.broker.receiver.EventReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class ChannelDeletionEventReceiver @Autowired constructor(private val standupDefinitionRepository: StandupDefinitionRepository,
                                                          private val standupRepository: StandupRepository) : EventReceiver {


    override fun supportsEvent(slackEvent: SlackEvent): Boolean {
        return (slackEvent.event["type"] == "channel_deleted" ||
                slackEvent.event["type"] == "channel_archive")
                && slackEvent.event.containsKey("channel")
    }

    override fun onReceiveEvent(slackEvent: SlackEvent, headers: HttpHeaders, team: Team) {
        val channelId = slackEvent.event["channel"] as String

        standupDefinitionRepository.update(
                withStatus = setOf(StandupDefinition.Status.ACTIVE),
                withBroadcastChannels = setOf(channelId),
                withTeamId = team.teamId,
                update = Update.update(StandupDefinition.STATUS, StandupDefinition.Status.ARCHIVED)
        )

        standupRepository.update(
                withBroadcastChannelId = channelId,
                withStatus = setOf(Standup.Status.CREATED, Standup.Status.OPEN),
                update = Update.update(Standup.STATUS, Standup.Status.CANCELLED)

        )
    }
}