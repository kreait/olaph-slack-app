package com.kreait.bots.agile.domain.v2.repository

import com.kreait.bots.agile.domain.v2.data.StandupSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.DayOfWeek


interface StandupSpecRepository : MongoRepository<StandupSpec, String>, StandupSpecRepositoryCustom

interface StandupSpecRepositoryCustom {

    fun find(withOlaphAccountId: String? = null,
             withStatus: StandupSpec.Status? = null,
             withParticipantUserId: String? = null,
             onDayOfWeek: DayOfWeek? = null): List<StandupSpec>?
}

open class StandupSpecRepositoryCustomImpl(@Autowired private val template: MongoTemplate) : StandupSpecRepositoryCustom {

    override fun find(withOlaphAccountId: String?, withStatus: StandupSpec.Status?, withParticipantUserId: String?, onDayOfWeek: DayOfWeek?): List<StandupSpec>? {

        return template.find(
                Query.query(criteriaOf(withOlaphAccountId, withStatus, withParticipantUserId, onDayOfWeek)),
                StandupSpec::class.java
        )
    }

    private fun criteriaOf(withOlaphAccountId: String?, withStatus: StandupSpec.Status?, withParticipantUserId: String?, onDayOfWeek: DayOfWeek?): Criteria {
        val criteriaList = mutableListOf<Criteria>()

        withOlaphAccountId?.let { criteriaList.add(where(StandupSpec.ACCOUNT_ID).`is`(it)) }
        withParticipantUserId?.let { criteriaList.add(where(StandupSpec.PARTICIPANTS).elemMatch(where(StandupSpec.Participant.USER_ID).`is`(it))) }
        onDayOfWeek?.let { criteriaList.add(where(StandupSpec.DAYS).elemMatch(where(StandupSpec.Day.DAY_OF_WEEK).`is`(it))) }
        withStatus?.let { criteriaList.add(where(StandupSpec.STATUS).`is`(it)) }

        return if (criteriaList.isEmpty()) Criteria() else Criteria().andOperator(*criteriaList.toTypedArray())
    }

}