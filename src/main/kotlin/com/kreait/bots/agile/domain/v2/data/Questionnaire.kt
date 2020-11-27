package com.kreait.bots.agile.domain.v2.data

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 *  Questionnaire created from a [StandupSpec] that will be sent to a single user. Contains the questions and answers.
 *  @property id internal id
 *  @property standupSpecId id of the [StandupSpec] the object was created from
 *  @property lifecycleStatus part of the questionnaire lifecycle the object is in
 *  @property date the date the questionnaire was/ will be sent on
 *  @property time time, when the questionnaire was/ will be sent
 *  @property timezoneSource specifies, which timezone was used (from the standupSpec or the user)
 *  @property timestamp system time, when the questionnaire was/ will be sent. This helps a lot with calculations
 *  @property userId reference to the integrationUser that will receive the questions
 *  @property standupName name of the stand-up
 *  @property items list of questionnaire-items containing questions and answers
 *  @property timesRemindersSent how often the user was reminded to answer the questions
 *  @property broadcastChannelIds references to the channels that will receive the broadcast
 */
@Document(collection = Questionnaire.COLLECTION_NAME)
data class Questionnaire(
        @Id @Field(ID) val id: String? = null,
        @Field(STANDUP_SPEC_ID) val standupSpecId: String,
        @Field(LIFECYLCE_STATUS) val lifecycleStatus: LifecycleStatus,
        @Field(DATE) val date: LocalDate,
        @Field(TIME) val time: LocalTime,
        @Field(TIMEZONE) val timezone: ZoneId,
        @Field(TIMEZONE_SOURCE) val timezoneSource: TimezoneSource,
        @Field(TIMESTAMP) val timestamp: Instant,
        @Field(USER_ID) val userId: String,
        @Field(STANDUP_NAME) val standupName: String,
        @Field(ITEMS) val items: List<Item>,
        @Field(REMINDERS_SENT) val timesRemindersSent: Int,
        @Field(BROADCAST_CHANNEL_IDS) val broadcastChannelIds: List<String>
) {

    companion object {
        const val COLLECTION_NAME = "Questionnaires"
        const val ID = "id"
        const val STANDUP_SPEC_ID = "standupSpecId"
        const val LIFECYLCE_STATUS = "lifecycleStatus"
        const val DATE = "date"
        const val TIME = "time"
        const val TIMEZONE = "timezone"
        const val TIMEZONE_SOURCE = "timezoneSource"
        const val TIMESTAMP = "timestamp"
        const val USER_ID = "userId"
        const val STANDUP_NAME = "standupName"
        const val ITEMS = "items"
        const val REMINDERS_SENT = "remindersSent"
        const val BROADCAST_CHANNEL_IDS = "broadcastChannelIds"

        /**
         * Creates a [Questionnaire] from a standupSpec
         * @param standupSpec the standupSpec that will be used to create the Questionnaire
         * @param userId integrationUser for which the questionnaire shall be created
         * @param day the weekday for which the questionnaire shall be created
         * @return [Questionnaire] from given standupSpec for given user and day of week
         */
        fun of(standupSpec: StandupSpec, userId: String, day: StandupSpec.Day): Questionnaire {
            return Questionnaire(standupSpecId = standupSpec.id!!,
                    lifecycleStatus = LifecycleStatus.CREATED,
                    date = LocalDate.now(),
                    time = day.time,
                    timezone = standupSpec.timezone,
                    timezoneSource = TimezoneSource.STANDUP_SPEC,
                    timestamp = ZonedDateTime.of(LocalDate.now(), day.time, standupSpec.timezone).toInstant(),
                    userId = userId,
                    standupName = standupSpec.name,
                    items = standupSpec.questions.map { Item(question = Item.Question(Item.Question.Type.TEXT, it.text)) },
                    timesRemindersSent = 0,
                    broadcastChannelIds = standupSpec.broadcastChannelIds
            )
        }
    }

    /**
     * Part of the Questionnaire-Lifecycle
     */
    enum class LifecycleStatus {
        /**
         * The object has been created
         */
        CREATED,
        /**
         * The first question has been sent
         */
        OPEN,
        /**
         * All questions have been answered. The broadcast was sent
         */
        CLOSED,
        /**
         * The questionnaire was cancelled. There may be unanswered questions and the broadcast might not have been sent
         */
        CANCELLED
    }

    /**
     * Which timezone was used
     */
    enum class TimezoneSource {
        /**
         * user specific timezone
         */
        USER,
        /**
         * standup-spec-global timezone was used
         */
        STANDUP_SPEC
    }

    /**
     * Questionnaire item
     * @property id internal id used to make finding and altering items easier
     * @property question the question
     * @property answer answer to the question
     */
    data class Item(
            @Field(ID) val id: String = ObjectId.get().toHexString(),
            @Field(QUESTION) val question: Question,
            @Field(ANSWER) val answer: Answer? = null
    ) {
        companion object {
            const val ID = "_id"
            const val QUESTION = "question"
            const val ANSWER = "answer"
        }

        /**
         * Whether the item has an answer
         * @return true, if the items question was answered
         */
        fun isAnswered() = answer != null

        /**
         * Question that was/ will be sent to the user
         * @property type type of question (eg. text-question)
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
             * Type of question
             */
            enum class Type {
                /**
                 * Simple text question
                 */
                TEXT
            }

        }

        /**
         * Answer sent by a user
         * @property value the answer value
         */
        data class Answer(
                @Field(VALUE) val value: String
        ) {
            companion object {
                const val VALUE = "value"
            }

        }
    }
}