package com.kreait.bots.agile.domain.slack.standupDefinition.join

import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.broker.receiver.EventReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import java.time.DayOfWeek

@Service
class MemberJoinedChannelReceiver @Autowired constructor(private val standupDefinitionRepository: StandupDefinitionRepository,
                                                         private val messageSender: SlackMessageSender) : EventReceiver {
    companion object {
        /**
         * Adds padding to hour and minute if needed
         */
        private fun transformTime(hour: Int, minute: Int): String {
            return "$hour".padStart(2, '0') + "$minute".padEnd(2, '0')
        }

        /**
         * Creates a readable string out of list of days
         */
        private fun transformDays(days: List<DayOfWeek>): String {
            return days.joinToString(", ") { it -> it.name.substring(0, 3).toLowerCase().capitalize() }
        }
    }


    override fun supportsEvent(slackEvent: SlackEvent): Boolean {
        return slackEvent.event["type"] == "member_joined_channel"
                && slackEvent.event.containsKey("user")
                && slackEvent.event.containsKey("channel")
                && slackEvent.event.containsKey("team")
    }

    override fun onReceiveEvent(slackEvent: SlackEvent, headers: HttpHeaders, team: Team) {

        val teamId = slackEvent.event["team"] as String
        val channelId = slackEvent.event["channel"] as String
        val userId = slackEvent.event["user"] as String

        val updatedActiveStandupDefinitions = this.standupDefinitionRepository.find(withTeamId = teamId)
                .filter { it.broadcastChannelId.equals(channelId) }
                .filter { userId !in it.subscribedUserIds }
                .map {
                    //TODO use update method in repository
                    this.standupDefinitionRepository.save(it.copy(subscribedUserIds = it.subscribedUserIds.plus(userId)))
                }.filter { it.status == StandupDefinition.Status.ACTIVE }

        if (updatedActiveStandupDefinitions.isNotEmpty()) {
            this.messageSender.sendEphemeralMessage(
                    responseType = ResponseType.CHANNEL_JOIN,
                    attachments = updatedActiveStandupDefinitions.map {
                        Attachment(
                                title = it.name,
                                text = "At ${transformTime(it.time.hour, it.time.minute)} on ${transformDays(it.days)}",
                                fallback = it.name
                        )
                    },
                    messageContext = MessageContext(channelId = channelId, teamId = teamId, userId = userId),
                    token = team.bot.accessToken
            )
        }
    }
}