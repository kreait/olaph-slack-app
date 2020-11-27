package com.kreait.bots.agile.domain.slack.standupDefinition.list

import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.common.messaging.Color
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters

@Service
class ListStandupsService @Autowired constructor(private val standupDefinitionRepository: StandupDefinitionRepository,
                                                 private val messageSender: SlackMessageSender,
                                                 private val userService: UserService) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ListStandupsService::class.java)
    }

    /**
     *  lists the standups in which the user participates/spectates
     */
    fun listStandups(slackCommand: SlackCommand, accessToken: String) {
        val userConversations = userService.conversationList(accessToken, slackCommand.userId).map { it.id }

        val now = LocalDateTime.now()

        val standupsSortedByDay = standupDefinitionRepository.find(withBroadcastChannels = userConversations.toSet(),
                withTeamId = slackCommand.teamId,
                withStatus = setOf(StandupDefinition.Status.ACTIVE)
        ).sortedBy {
            DayHelper.nextStandupDateTime(it.days, it.time, it.offset.toLong(), now)
        }

        val attachments: MutableList<Attachment> = mutableListOf()
        standupsSortedByDay.forEach {

            attachments.add(
                    Attachment(
                            title = it.name, text = "The next one is ${DayHelper.getNextStandupDay(it.days, it.time, it.offset.toLong(), now)} at ${it.time}",
                            color = if (it.subscribedUserIds.contains(slackCommand.userId)) Color.GOOD else Color.NEUTRAL,
                            fallback = "standup ${it.name}")
            )
        }
        if (standupsSortedByDay.isEmpty())
            messageSender.sendEphemeralMessage(ResponseType.NO_STANDUPS_FOUND, listOf(), MessageContext.Companion.of(slackCommand), accessToken)
        else
            messageSender.sendCustomEphemeralMessage(attachments, MessageContext.of(slackCommand), accessToken, "These are your stand-ups: ")
    }

}

object DayHelper {


    fun getNextStandupDay(standupDays: List<DayOfWeek>, standupTime: LocalTime, userOffset: Long, today: LocalDateTime): String {
        val dateTime = today.plusSeconds(userOffset)

        val nextStandupDay = nextStandupDateTime(standupDays, standupTime, userOffset, today)

        val distance = Math.abs(Duration.between(dateTime, nextStandupDay).toDays())
        return if (dateTime.dayOfWeek == nextStandupDay.dayOfWeek && distance == 0L) {
            "today"
        } else if (dateTime.dayOfWeek == nextStandupDay.dayOfWeek && distance > 0L) {
            "next ${dateTime.dayOfWeek.name.toLowerCase().capitalize()}"
        } else if (dateTime.plusDays(1).dayOfWeek == nextStandupDay.dayOfWeek) {
            "tomorrow"
        } else {
            nextStandupDay.dayOfWeek.name.toLowerCase().capitalize()
        }

    }

    fun nextStandupDateTime(standupDays: List<DayOfWeek>, standupTime: LocalTime, userOffset: Long, today: LocalDateTime): LocalDateTime {
        val dateTime = today.plusSeconds(userOffset)
        return standupDays.map {

            // if the standup time for today has already passed we only check the next day
            if (dateTime.toLocalTime().isBefore(standupTime)) {
                dateTime.with(TemporalAdjusters.nextOrSame(it))
            } else {
                dateTime.with(TemporalAdjusters.next(it))
            }
        }.sortedBy { it }.first()
    }

}
