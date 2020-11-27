package com.kreait.bots.agile.domain.common.data

import org.springframework.data.mongodb.core.query.Criteria
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime


class StandupCriteria {

    companion object {
        fun of(id: String? = null,
               standupDefinitionId: String? = null,
               status: Standup.Status? = null,
               notStatus: Set<Standup.Status>? = null,
               date: LocalDate? = null,
               dateLt: LocalDate? = null,
               time: LocalTime? = null,
               timeLte: LocalTime? = null,
               userId: String? = null,
               broadcastChannelId: String? = null,
               questions: List<String>? = null,
               questionsAsked: Int? = null,
               answers: List<String>? = null,
               answer: Standup.Answer? = null,
               name: String? = null,
               timestampOlderThan: Instant? = null,
               timestampNewerThan: Instant? = null
        ): Criteria {
            val criteriaList = mutableListOf<Criteria>()

            id?.let { criteriaList.add(Criteria.where("id").`is`(it)) }

            standupDefinitionId?.let { criteriaList.add(Criteria.where(Standup.STANDUP_DEFINITION_ID).`is`(it)) }

            status?.let { criteriaList.add(Criteria.where(Standup.STATUS).`is`(it)) }

            notStatus?.let { criteriaList.add(Criteria.where(Standup.STATUS).nin(it)) }

            date?.let { criteriaList.add(Criteria.where(Standup.DATE).`is`(it)) }

            dateLt?.let { criteriaList.add(Criteria.where(Standup.DATE).lt(it)) }

            time?.let { criteriaList.add(Criteria.where(Standup.TIME).`is`(it)) }

            timeLte?.let { criteriaList.add(Criteria.where(Standup.TIME).lte(it)) }

            userId?.let { criteriaList.add(Criteria.where(Standup.USER_ID).`is`(it)) }

            timestampOlderThan?.let { criteriaList.add(Criteria.where(Standup.TIMESTAMP).lt(it)) }

            timestampNewerThan?.let { criteriaList.add(Criteria.where(Standup.TIMESTAMP).gt(it)) }

            if (questions != null && questions.isNotEmpty()) {
                criteriaList.add(Criteria.where(Standup.QUESTIONS).all(questions))
            }

            broadcastChannelId?.let { criteriaList.add(Criteria.where(Standup.BROADCAST_CHANNEL_ID).`is`(broadcastChannelId)) }

            questionsAsked?.let { criteriaList.add(Criteria.where(Standup.QUESTIONS_ASKED).`is`(it)) }

            if (answers != null && answers.isNotEmpty()) {
                criteriaList.add(Criteria.where(Standup.ANSWERS).elemMatch(Criteria.where(Standup.Answer.TEXT).`in`(answers)))
            }

            answer?.let { criteriaList.add(Criteria.where(Standup.ANSWERS).`is`(it)) }

            name?.let { criteriaList.add(Criteria.where(Standup.NAME).`is`(it)) }

            return if (criteriaList.isEmpty()) Criteria() else Criteria().andOperator(*criteriaList.toTypedArray())

        }
    }
}
