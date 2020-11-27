package com.kreait.bots.agile.core.standup.common

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.Standup.Status
import com.kreait.bots.agile.domain.common.data.StandupCriteria
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.springframework.data.mongodb.core.query.Criteria
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * creates a [Standup] example with default mock data
 */
fun Standup.Companion.example(standupDefinitionId: String = "standupDefinitionId", status: Status = Status.CREATED, date: LocalDate = LocalDate.now(),
                              time: LocalTime = LocalTime.of(1, 0), userId: String = "userId", broadcastChannelId: String = "broadcastChannelId", questions: List<String> = listOf(),
                              questionsAsked: Int = 0, answers: List<Standup.Answer> = listOf(), name: String = "name", timestamp: Instant = LocalDateTime.of(date, time).toInstant(ZoneOffset.UTC).minusSeconds(0)
): Standup {
    return Standup(standupDefinitionId = standupDefinitionId, status = status, date = date, time = time, userId = userId,
            broadcastChannelId = broadcastChannelId, questions = questions, questionsAsked = questionsAsked, answers = answers, name = name,
            timestamp = timestamp)
}

/**
 * creates a [Standup.Answer] example with default mock data
 */

fun Standup.Answer.Companion.example(text: String = "text", eventId: String = "eventId", eventTime: Int = 0): Standup.Answer {
    return Standup.Answer(text = text, eventId = eventId, eventTime = eventTime)
}

/**
 * creates a [Criteria] object to [Query] [StandupRepository]
 */

fun StandupCriteria.Companion.example(
        standupDefinitionId: String? = null, status: Status? = null, date: LocalDate? = null, dateOlderThan: LocalDate? = null,
        time: LocalTime? = null, timeOlderEquals: LocalTime? = null, userId: String? = null, broadcastChannelId: String? = null,
        questions: List<String>? = null, questionsAsked: Int? = null, answers: List<String>? = null, answer: Standup.Answer? = null,
        name: String? = null, id: String? = null
): Criteria {

    return StandupCriteria.of(id = id, standupDefinitionId = standupDefinitionId, status = status, date = date,
            dateLt = dateOlderThan, time = time, timeLte = timeOlderEquals, userId = userId, broadcastChannelId = broadcastChannelId,
            questions = questions, questionsAsked = questionsAsked, answers = answers, answer = answer, name = name)
}
