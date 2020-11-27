package com.kreait.bots.agile.domain.slack.standupDefinition.leave

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

/**
 * Event Receiver that reacts on the users leaving a channel
 */
@Service
class MemberLeftEventReceiver @Autowired constructor(private val standupDefinitionRepository: StandupDefinitionRepository,
                                                     private val standupRepository: StandupRepository,
                                                     private val memberLeftMessageSender: MemberLeftMessageSender) : EventReceiver {


    /**
     * Supports "member_left_channel" event
     *
     * Reference [https://api.slack.com/events/member_left_channel]
     */
    override fun supportsEvent(slackEvent: SlackEvent): Boolean {
        return slackEvent.event["type"] == "member_left_channel"
                && slackEvent.event.containsKey("user")
                && slackEvent.event.containsKey("channel")
                && slackEvent.event.containsKey("team")
    }

    /**
     * on receive event the leaving member will be removed from the standup and open standups will in that channel will leave
     * SideEffects:
     */
    override fun onReceiveEvent(slackEvent: SlackEvent, headers: HttpHeaders, team: Team) {
        val teamId = slackEvent.event["team"] as String
        val channelId = slackEvent.event["channel"] as String
        val userId = slackEvent.event["user"] as String


        val standupDefinitions = this.standupDefinitionRepository.find(
                withTeamId = teamId,
                withBroadcastChannels = setOf(channelId),
                withStatus = setOf(StandupDefinition.Status.ACTIVE),
                withSubscribedUserIds = listOf(userId)
        )

        this.removeUserFromStandup(teamId, channelId, userId)

        standupDefinitions.forEach { it ->
            if (hasOpenStandups(userId, it.id!!)) {
                memberLeftMessageSender.sendCancelOpenStandupMessage(userId, it, team.bot.accessToken)
            }
            closeAllStandups(userId, it.id)
        }

        memberLeftMessageSender.sendChannelLeftMessage(userId, standupDefinitions, channelId, team.bot.accessToken)
    }


    private fun removeUserFromStandup(teamId: String, channelId: String, userId: String) {
        this.standupDefinitionRepository.update(
                withTeamId = teamId,
                withBroadcastChannels = setOf(channelId),
                withStatus = setOf(StandupDefinition.Status.ACTIVE),
                withSubscribedUserIds = listOf(userId),
                update = Update().pull(StandupDefinition.SUBSCRIBED_USER_IDS, userId))
    }

    private fun hasOpenStandups(userId: String, standupDefinitionId: String): Boolean {
        return !standupRepository.find(withUserIds = setOf(userId), withStandupDefinitionId = standupDefinitionId).none { it.status == Standup.Status.OPEN }
    }

    /**
     * Sets all Standups for given [com.kreait.bots.agile.domain.common.data.StandupDefinition] and [User] to [Standup.Status.CLOSED]
     *
     * SideEffects:
     *  - Overrides Cancelled Standups
     */
    private fun closeAllStandups(userId: String, standupDefinitionId: String) {
        standupRepository.update(withUserIds = setOf(userId),
                withStandupDefinitionId = standupDefinitionId,
                update = Update.update(Standup.STATUS, Standup.Status.CLOSED)
        )
    }
}
