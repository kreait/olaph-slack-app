package com.kreait.bots.agile.domain.v2.data

import com.kreait.bots.olaph.dto.jackson.common.StandupSpecRequest
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId

/**
 * Specifies how a stand-up takes place
 * @property id internal id
 * @property name name of the stand-up
 * @property status whether the stand-up takes place or not. Used to archive entities instead of deleting them
 * @property participants list of participants of the stand-up
 * @property questions list of questions that will be sent to the participants
 * @property timezone timezone of the stand-up
 * @property days list of nested objects that specify when the stand-up will take place
 * @property broadcastChannelIds references the channels that will receive the broadcast
 * @property authorId references the user that created the stand-up
 * @property accountId references the Olaph account the object belongs to
 */
@Document(collection = StandupSpec.COLLECTION_NAME)
data class StandupSpec(
        @Id @Field(ID) val id: String? = null,
        @Field(NAME) val name: String,
        @Field(STATUS) val status: Status = Status.ACTIVE,
        @Field(PARTICIPANTS) val participants: List<Participant>,
        @Field(QUESTIONS) val questions: List<Question>,
        @Field(TIMEZONE) val timezone: ZoneId,
        @Field(DAYS) val days: List<Day>,
        @Field(BROADCAST_CHANNEL_IDS) val broadcastChannelIds: List<String>,
        @Field(AUTHOR_ID) val authorId: String,
        @Field(ACCOUNT_ID) val accountId: String
) {

    companion object {
        const val COLLECTION_NAME = "standupSpecs"
        const val ID = "id"
        const val NAME = "name"
        const val STATUS = "status"
        const val PARTICIPANTS = "participants"
        const val QUESTIONS = "questions"
        const val TIMEZONE = "timezone"
        const val DAYS = "days"
        const val BROADCAST_CHANNEL_IDS = "broadcastChannelIds"
        const val AUTHOR_ID = "authorId"
        const val ACCOUNT_ID = "accountId"

        /**
         * Creates a [StandupSpec] from a dto
         * @param dto the dto to create a [StandupSpec] from
         * @return [StandupSpec] containing the data from the dto
         */
        fun of(dto: StandupSpecRequest): StandupSpec {
            return StandupSpec(
                    name = dto.name,
                    participants = dto.participants.map {
                        Participant(
                                timezone = it.timezone,
                                userId = it.userId
                        )
                    },
                    questions = dto.questions.map {
                        Question(
                                type = when (it.type) {
                                    com.kreait.bots.olaph.dto.jackson.common.Question.Type.TEXT -> Question.Type.TEXT
                                },
                                text = it.text
                        )
                    },
                    timezone = dto.timezone,
                    broadcastChannelIds = dto.broadcastChannelIds,
                    days = dto.days.map { Day(dayOfWeek = it.dayOfWeek, time = it.time) },
                    authorId = dto.authorId,
                    accountId = dto.accountId
            )
        }
    }

    /**
     * Whether the object is archived or in use
     */
    enum class Status {
        /**
         * The object is in use
         */
        ACTIVE,
        /**
         * The object is archived. The stand-up shall not take place
         */
        ARCHIVED
    }

    /**
     * Participant of a stand-up
     * @property id internal id. Necessary to edit participants via REST
     * @property timezone timezone of the participant. If this field is present, this timezone will be used instead of
     * the one found in the parent object
     * @property userId integration user id of participants
     */
    data class Participant(
            @Id @Field(ID) val id: String? = ObjectId().toHexString(),
            @Field(TIMEZONE) val timezone: ZoneId?,
            @Field(USER_ID) val userId: String
    ) {
        companion object {
            const val ID = "id"
            const val TIMEZONE = "timezone"
            const val USER_ID = "userId"
        }
    }

    /**
     * Question of a stand-up
     * @property type type of question
     * @property text question text
     */
    data class Question(
            @Field(TYPE) val type: Type,
            @Field(TEXT) val text: String
    ) {
        companion object {
            const val TYPE = "type"
            const val TEXT = "text"
        }

        /**
         * The type of question
         */
        enum class Type {
            /**
             * Simple text question
             */
            TEXT
        }
    }

    /**
     * Day (and time) on which the stand-up will take place
     * @property dayOfWeek day of week
     * @property time time
     */
    data class Day(
            @Field(DAY_OF_WEEK) val dayOfWeek: DayOfWeek,
            @Field(TIME) val time: LocalTime
    ) {
        companion object {
            const val DAY_OF_WEEK = "dayOfWeek"
            const val TIME = "time"
        }
    }

}

