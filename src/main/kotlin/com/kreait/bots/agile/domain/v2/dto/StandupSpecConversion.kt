package com.kreait.bots.agile.domain.v2.dto


import com.kreait.bots.agile.domain.v2.data.StandupSpec
import com.kreait.bots.olaph.dto.jackson.common.Day
import com.kreait.bots.olaph.dto.jackson.common.ParticipantResponse
import com.kreait.bots.olaph.dto.jackson.common.Question
import com.kreait.bots.olaph.dto.jackson.common.StandupSpecResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.SuccessfulStandupSpecResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.SuccessfulListStandupResponse

/**
 * Creates a [StandupSpecResponse] from [StandupSpec] entity
 * @param entity the entity that should be turned into a dto
 * @return the [StandupSpecResponse] containing the entities data
 */
fun SuccessfulStandupSpecResponse.Companion.of(entity: StandupSpec): SuccessfulStandupSpecResponse {
    return SuccessfulStandupSpecResponse(
            true,
            StandupSpecResponse(
                    id = entity.id!!,
                    name = entity.name,
                    participants = entity.participants.map {
                        ParticipantResponse(
                                id = it.id!!,
                                timezone = it.timezone,
                                userId = it.userId)
                    },
                    questions = entity.questions.map {
                        Question(
                                type = when (it.type) {
                                    StandupSpec.Question.Type.TEXT -> Question.Type.TEXT
                                },
                                text = it.text
                        )
                    },
                    timezone = entity.timezone,
                    days = entity.days.map {
                        Day(
                                dayOfWeek = it.dayOfWeek,
                                time = it.time)
                    },
                    broadcastChannelIds = entity.broadcastChannelIds,
                    authorId = entity.authorId,
                    accountId = entity.accountId)
    )
}


fun SuccessfulListStandupResponse.Companion.of(standupSpecs: List<StandupSpec>): SuccessfulListStandupResponse {
    return SuccessfulListStandupResponse(
            true,
            standupSpecs.map { entity ->
                StandupSpecResponse(
                        id = entity.id!!,
                        name = entity.name,
                        participants = entity.participants.map {
                            ParticipantResponse(
                                    id = it.id!!,
                                    timezone = it.timezone,
                                    userId = it.userId)
                        },
                        questions = entity.questions.map {
                            Question(
                                    type = when (it.type) {
                                        StandupSpec.Question.Type.TEXT -> Question.Type.TEXT
                                    },
                                    text = it.text
                            )
                        },
                        timezone = entity.timezone,
                        days = entity.days.map {
                            Day(
                                    dayOfWeek = it.dayOfWeek,
                                    time = it.time)
                        },
                        broadcastChannelIds = entity.broadcastChannelIds,
                        authorId = entity.authorId,
                        accountId = entity.accountId)
            }

    )
}
