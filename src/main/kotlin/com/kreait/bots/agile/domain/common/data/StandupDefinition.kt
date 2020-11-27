package com.kreait.bots.agile.domain.common.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

/**
 * Object defining basic parameters of a standup
 */
@Document(collection = StandupDefinition.COLLECTION_NAME)
data class StandupDefinition(@Id @Field(ID) val id: String? = null,
                             @Field(NAME) val name: String,
                             @Field(DAYS) val days: List<DayOfWeek>,
                             @Field(TIME) val time: LocalTime,
                             @Field(BROADCAST_CHANNEL_ID) val broadcastChannelId: String,
                             @Field(SUBSCRIBED_USER_IDS) val subscribedUserIds: List<String>,
                             @Field(QUESTIONS) val questions: List<String>,
                             @Field(TEAM_ID) @Indexed val teamId: String,
                             @Field(STATUS) @Indexed val status: Status = Status.ACTIVE,
                             @Field(CREATED_BY) val createdBy: String? = null,
                             @Field(CREATED_AT) val createdAt: Instant? = null,
                             @Field(MODIFIED_AT) val modifiedAt: Instant? = null,
                             @Field(TIMEZONE_OFFSET) var offset: Int = 0) {
    enum class Status {
        ACTIVE,
        ARCHIVED
    }

    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val DAYS = "days"
        const val TIME = "time"
        const val BROADCAST_CHANNEL_ID = "broadcastChannelId"
        const val SUBSCRIBED_USER_IDS = "subscribedUserIds"
        const val QUESTIONS = "questions"
        const val TEAM_ID = "teamId"
        const val STATUS = "status"
        const val CREATED_BY = "createdBy"
        const val CREATED_AT = "createdAt"
        const val MODIFIED_AT = "modifiedAt"
        const val MODIFIED_BY = "modifiedBy"
        const val TIMEZONE_OFFSET = "timezoneOffset"

        const val MEMBER_COLLECTION = "members"
        const val STANDUP_DEFINITION_COUNT = "standupDefinitions"
        const val COLLECTION_NAME = "standupDefinition"


    }
}
