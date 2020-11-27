package com.kreait.bots.agile.domain.v2.repository

import com.kreait.bots.agile.domain.v2.data.Questionnaire
import com.kreait.bots.agile.domain.v2.exception.NonUniqueResultException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

interface QuestionnaireRepository : MongoRepository<Questionnaire, String>, QuestionnaireRepositoryCustom

interface QuestionnaireRepositoryCustom {
    fun find(withName: String? = null,
             withStandupSpecId: String? = null,
             withBeforeTimestamp: Instant? = null,
             withBeforeTime: LocalTime? = null,
             withAfterTime: LocalTime? = null,
             withDate: LocalDate? = null,
             withUserId: String? = null,
             withLifeCycleStatus: Questionnaire.LifecycleStatus? = null): List<Questionnaire>

    fun findOne(withName: String? = null,
                withStandupSpecId: String? = null,
                withBeforeTimestamp: Instant? = null,
                withBeforeTime: LocalTime? = null,
                withAfterTime: LocalTime? = null,
                withDate: LocalDate? = null,
                withUserId: String? = null,
                withLifeCycleStatus: Questionnaire.LifecycleStatus? = null): Questionnaire

    fun findOneOrNull(withName: String? = null,
                      withStandupSpecId: String? = null,
                      withBeforeTimestamp: Instant? = null,
                      withBeforeTime: LocalTime? = null,
                      withAfterTime: LocalTime? = null,
                      withDate: LocalDate? = null,
                      withUserId: String? = null,
                      withLifeCycleStatus: Questionnaire.LifecycleStatus? = null): Questionnaire?

    fun exists(withName: String? = null,
               withStandupSpecId: String? = null,
               withBeforeTimestamp: Instant? = null,
               withBeforeTime: LocalTime? = null,
               withAfterTime: LocalTime? = null,
               withDate: LocalDate? = null,
               withUserId: String? = null,
               withLifeCycleStatus: Questionnaire.LifecycleStatus? = null): Boolean

    fun changeLifecycleStatus(id: String, status: Questionnaire.LifecycleStatus)

}

open class QuestionnaireRepositoryCustomImpl constructor(@Autowired private val template: MongoTemplate) : QuestionnaireRepositoryCustom {

    override fun exists(withName: String?, withStandupSpecId: String?, withBeforeTimestamp: Instant?, withBeforeTime: LocalTime?, withAfterTime: LocalTime?, withDate: LocalDate?, withUserId: String?, withLifeCycleStatus: Questionnaire.LifecycleStatus?): Boolean {
        return template.exists(
                Query.query(criteriaOf(withName, withStandupSpecId, withBeforeTimestamp, withBeforeTime, withAfterTime, withDate, withUserId, withLifeCycleStatus)),
                Questionnaire.COLLECTION_NAME
        )
    }

    override fun changeLifecycleStatus(id: String, lifecycleStatus: Questionnaire.LifecycleStatus) {
        val criteria = Criteria.where(Questionnaire.ID).`is`(id)

        val update = Update().set(Questionnaire.LIFECYLCE_STATUS, lifecycleStatus)

        template.updateFirst(Query.query(criteria), update, Questionnaire::class.java)
    }

    override fun find(withName: String?, withStandupSpecId: String?, withBeforeTimestamp: Instant?, withBeforeTime: LocalTime?, withAfterTime: LocalTime?, withDate: LocalDate?, withUserId: String?, withLifeCycleStatus: Questionnaire.LifecycleStatus?): List<Questionnaire> {
        return template.find(
                Query.query(criteriaOf(withName, withStandupSpecId, withBeforeTimestamp, withBeforeTime, withAfterTime, withDate, withUserId, withLifeCycleStatus)),
                Questionnaire::class.java)
    }

    override fun findOne(withName: String?, withStandupSpecId: String?, withBeforeTimestamp: Instant?, withBeforeTime: LocalTime?, withAfterTime: LocalTime?, withDate: LocalDate?, withUserId: String?, withLifeCycleStatus: Questionnaire.LifecycleStatus?): Questionnaire {
        val find = find(withName, withStandupSpecId, withBeforeTimestamp, withBeforeTime, withAfterTime, withDate, withUserId, withLifeCycleStatus)
        return if (find.size == 1) find.first() else throw NonUniqueResultException()
    }

    override fun findOneOrNull(withName: String?, withStandupSpecId: String?, withBeforeTimestamp: Instant?, withBeforeTime: LocalTime?, withAfterTime: LocalTime?, withDate: LocalDate?, withUserId: String?, withLifeCycleStatus: Questionnaire.LifecycleStatus?)
            : Questionnaire? {
        val find = find(withName, withStandupSpecId, withBeforeTimestamp, withBeforeTime, withAfterTime, withDate, withUserId, withLifeCycleStatus)
        return when {
            find.size == 1 -> find.first()
            find.size > 1 -> throw NonUniqueResultException()
            else -> null
        }
    }

    private fun criteriaOf(withName: String? = null,
                           withStandupSpecId: String?,
                           withBeforeTimestamp: Instant? = null,
                           withBeforeTime: LocalTime? = null,
                           withAfterTime: LocalTime? = null,
                           withDate: LocalDate? = null,
                           withUserId: String? = null,
                           withLifeCycleStatus: Questionnaire.LifecycleStatus? = null
    ): Criteria {
        val criteriaList = mutableListOf<Criteria>()
        withName?.let { criteriaList.add(where(Questionnaire.STANDUP_NAME).`is`(it)) }
        withStandupSpecId?.let { criteriaList.add(where(Questionnaire.STANDUP_SPEC_ID).`is`(it)) }
        withBeforeTimestamp?.let { criteriaList.add(where(Questionnaire.TIMESTAMP).lt(it)) }
        withBeforeTime?.let { criteriaList.add(where(Questionnaire.TIME).lt(it)) }
        withAfterTime?.let { criteriaList.add(where(Questionnaire.TIME).gt(it)) }
        withDate?.let { criteriaList.add(where(Questionnaire.DATE).`is`(it)) }
        withUserId?.let { criteriaList.add(where(Questionnaire.USER_ID).`is`(it)) }
        withLifeCycleStatus?.let { criteriaList.add(where(Questionnaire.LIFECYLCE_STATUS).`is`(it)) }
        return if (criteriaList.isEmpty()) Criteria() else Criteria().andOperator(*criteriaList.toTypedArray())
    }

}
