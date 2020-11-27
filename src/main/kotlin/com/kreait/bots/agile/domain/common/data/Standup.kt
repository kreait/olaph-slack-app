package com.kreait.bots.agile.domain.common.data

import com.kreait.bots.agile.domain.common.data.Standup.Status.CANCELLED
import com.kreait.bots.agile.domain.common.data.Standup.Status.CLOSED
import com.kreait.bots.agile.domain.common.data.Standup.Status.CREATED
import com.kreait.bots.agile.domain.common.data.Standup.Status.OPEN
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * this Object contains all questionnaires from a standup of a certain day
 * will be periodically created and updated based on a [StandupDefinition]
 */
@Document(collection = Standup.COLLECTION_NAME)
data class Standup(@Id val id: String? = null,
                   @Field(STANDUP_DEFINITION_ID) @Indexed val standupDefinitionId: String,
                   @Field(STATUS) @Indexed val status: Status = Status.CREATED,
                   @Field(DATE) val date: LocalDate = LocalDate.now(),
                   @Field(TIME) val time: LocalTime = LocalTime.now(),
                   @Field(USER_ID) val userId: String,
                   @Field(BROADCAST_CHANNEL_ID) val broadcastChannelId: String,
                   @Field(QUESTIONS) val questions: List<String>,
                   @Field(QUESTIONS_ASKED) val questionsAsked: Int = 0,
                   @Field(ANSWERS) val answers: List<Answer> = listOf(),
                   @Field(NAME) val name: String,
                   @Field(TIMESTAMP) val timestamp: Instant = Instant.now(),
                   @Field(TEAM_ID) val teamId: String = "",
                   @Field(REMINDED) val reminded: Boolean = false) {

    data class Answer(@Field(TEXT) val text: String, @Field(EVENT_ID) val eventId: String, @Field(EVENT_TIME) val eventTime: Int) {
        companion object {
            const val TEXT = "text"
            const val EVENT_ID = "eventId"
            const val EVENT_TIME = "eventTime"
        }
    }

    /**
     * infers whether all questions of a standup are answered
     * and/or the result was broadcasted to a channel
     *
     * @property CREATED [Standup] has been created
     * @property OPEN first question has been send
     * @property CLOSED [Standup.ANSWERS] have been broadcasted to [Standup.BROADCAST_CHANNEL_ID]
     * @property CANCELLED [Standup] has been cancelled
     */
    enum class Status {
        CREATED,
        OPEN,
        CLOSED,
        CANCELLED
    }

    companion object {
        const val STANDUP_DEFINITION_ID = "standupDefinitionId"
        const val STATUS = "status"
        const val DATE = "date"
        const val TIME = "time"
        const val COLLECTION_NAME = "standups"
        const val USER_ID = "userId"
        const val BROADCAST_CHANNEL_ID = "broadcastChannelId"
        const val QUESTIONS = "questions"
        const val QUESTIONS_ASKED = "questionsAsked"
        const val ANSWERS = "answers"
        const val NAME = "name"
        const val ANSWERED_COLLECTION = "answersAmount"
        const val TIMESTAMP = "timestamp"
        const val TEAM_ID = "teamId"
        const val REMINDED = "reminded"
        /**
         * create a [Standup] from its [StandupDefinition]
         * the [timestamp] describes the time, when the standup should be triggered in UTC-0
         * [offset] the offset in seconds according to UTC-0
         */
        fun of(standupDefinition: StandupDefinition, userId: String): Standup {
            return Standup(
                    standupDefinitionId = standupDefinition.id!!,
                    time = standupDefinition.time,
                    userId = userId,
                    broadcastChannelId = standupDefinition.broadcastChannelId,
                    questions = standupDefinition.questions,
                    name = standupDefinition.name,
                    timestamp = LocalDateTime.of(LocalDate.now(), standupDefinition.time).toInstant(ZoneOffset.UTC).minusSeconds(standupDefinition.offset.toLong()),
                    teamId = standupDefinition.teamId)
        }
    }

    class Predicates constructor(val standupRepository: StandupRepository) {
        companion object
    }

}

