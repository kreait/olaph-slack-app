package com.kreait.bots.agile.domain.common.data

import com.kreait.bots.agile.domain.common.actuator.ArrayCount
import com.kreait.bots.agile.domain.slack.standup.statistics.DailyActiveUsers
import com.mongodb.client.result.UpdateResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.count
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


interface StandupRepository : MongoRepository<Standup, String>, StandupRepositoryCustom {

}

interface StandupRepositoryCustom {


    fun getDailyActiveUsers(dateRange: List<LocalDate>): List<DailyActiveUsers>


    fun findAnsweredAndOpen(userId: String? = null): List<Standup>

    fun exists(withUserIds: Set<String>? = null,
               withStatus: Set<Standup.Status>? = null,
               withStandupDefinitionId: String? = null,
               isOnDate: LocalDate? = null,
               hasAnswer: Standup.Answer? = null): Boolean

    /**
     * Finds Standup objects by different criterias
     */
    fun find(withIds: Set<String>? = null,
             withStandupDefinitionId: String? = null,
             withStatus: Set<Standup.Status>? = null,
             withoutStatus: Set<Standup.Status>? = null,
             isOnDate: LocalDate? = null,
             isBeforeDate: LocalDate? = null,
             isAfterDate: LocalDate? = null,
             isOnTime: LocalTime? = null,
             isBeforeOrOnTime: LocalTime? = null,
             withUserIds: Set<String>? = null,
             withBroadcastChannelId: String? = null,
             hasNumberOfAskedQuestions: Int? = null,
             timestampIsBefore: Instant? = null,
             timestampIsAfter: Instant? = null,
             hasAnswer: Standup.Answer? = null,
             offset: Long? = null,
             limit: Int? = null): List<Standup>

    /**
     * Finds Standup objects by different criterias
     */
    fun update(withIds: Set<String>? = null,
               withStandupDefinitionId: String? = null,
               withStatus: Set<Standup.Status>? = null,
               withoutStatus: Set<Standup.Status>? = null,
               isOnDate: LocalDate? = null,
               isBeforeDate: LocalDate? = null,
               isAfterDate: LocalDate? = null,
               isOnTime: LocalTime? = null,
               isBeforeOrOnTime: LocalTime? = null,
               withUserIds: Set<String>? = null,
               withBroadcastChannelId: String? = null,
               hasNumberOfAskedQuestions: Int? = null,
               timestampIsBefore: Instant? = null,
               timestampIsAfter: Instant? = null,
               hasAnswer: Standup.Answer? = null,
               update: Update)


    fun findStandups(query: Query): List<Standup>

    /**
     * updates a [Standup]
     */
    fun updateStandup(query: Query, update: Update): Standup?

    /**
     * updates a list of [Standup]s
     */
    fun updateStandups(query: Query, update: Update): UpdateResult

    /**
     * checks if a received answer already exists
     * @return true if answer exists, otherwise false
     */
    fun existsByCriteria(criteria: Criteria): Boolean

    fun getAnsweredStandups(): String
}

open class StandupRepositoryImpl constructor(@Autowired private val template: MongoTemplate) : StandupRepositoryCustom {


    override fun getDailyActiveUsers(dateRange: List<LocalDate>): List<DailyActiveUsers> {
        val dates = dateRange.map { it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
        val agg = newAggregation(
                match(criteriaOf(withStatus = setOf(Standup.Status.CLOSED), isInDates = dates)),
                group(Standup.USER_ID),
                count().`as`("amount")
        )

        return template.aggregate(agg, Standup.COLLECTION_NAME, DailyActiveUsers::class.java).mappedResults
    }


    /**
     * Finds all Answered standups that are open (this is determined by questions amount and answer amount equality)
     */
    override fun findAnsweredAndOpen(userId: String?): List<Standup> {
        val query = BasicQuery("{ \$expr: { \$eq: [ { \$size: \"\$${Standup.QUESTIONS}\" }, { \$size: \"\$${Standup.ANSWERS}\" } ] } }")
        query.addCriteria(where(Standup.STATUS).`is`(Standup.Status.OPEN))
        userId?.let { query.addCriteria(where(Standup.USER_ID).`is`(userId)) }
        return template.find(query, Standup::class.java)
    }

    override fun exists(withUserIds: Set<String>?,
                        withStatus: Set<Standup.Status>?,
                        withStandupDefinitionId: String?,
                        isOnDate: LocalDate?,
                        hasAnswer: Standup.Answer?): Boolean {
        val criteria = criteriaOf(withStandupDefinitionId = withStandupDefinitionId, withStatus = withStatus, isOnDate = isOnDate,
                withUserIds = withUserIds, hasAnswer = hasAnswer)

        val query = Query.query(criteria)
        return template.exists(query, Standup::class.java)
    }

    override fun find(withIds: Set<String>?,
                      withStandupDefinitionId: String?,
                      withStatus: Set<Standup.Status>?,
                      withoutStatus: Set<Standup.Status>?,
                      isOnDate: LocalDate?,
                      isBeforeDate: LocalDate?,
                      isAfterDate: LocalDate?,
                      isOnTime: LocalTime?,
                      isBeforeOrOnTime: LocalTime?,
                      withUserIds: Set<String>?,
                      withBroadcastChannelId: String?,
                      hasNumberOfAskedQuestions: Int?,
                      timestampIsBefore: Instant?,
                      timestampIsAfter: Instant?,
                      hasAnswer: Standup.Answer?,
                      offset: Long?,
                      limit: Int?): List<Standup> {

        val criteria = criteriaOf(withIds, withStandupDefinitionId, withStatus, withoutStatus, isOnDate, isBeforeDate, isOnTime, isBeforeOrOnTime, withUserIds,
                withBroadcastChannelId, hasNumberOfAskedQuestions, timestampIsBefore, timestampIsAfter, hasAnswer, isAfterDate)


        val query = Query.query(criteria)
        offset?.let { query.skip(it) }
        limit?.let { query.limit(it) }

        return template.find(query, Standup::class.java)
    }

    override fun update(withIds: Set<String>?,
                        withStandupDefinitionId: String?,
                        withStatus: Set<Standup.Status>?,
                        withoutStatus: Set<Standup.Status>?,
                        isOnDate: LocalDate?,
                        isBeforeDate: LocalDate?,
                        isAfterDate: LocalDate?,
                        isOnTime: LocalTime?,
                        isBeforeOrOnTime: LocalTime?,
                        withUserIds: Set<String>?,
                        withBroadcastChannelId: String?,
                        hasNumberOfAskedQuestions: Int?,
                        timestampIsBefore: Instant?,
                        timestampIsAfter: Instant?,
                        hasAnswer: Standup.Answer?,
                        update: Update) {
        val criteria = criteriaOf(withIds, withStandupDefinitionId, withStatus, withoutStatus, isOnDate, isBeforeDate, isOnTime, isBeforeOrOnTime, withUserIds,
                withBroadcastChannelId, hasNumberOfAskedQuestions, timestampIsBefore, timestampIsAfter, hasAnswer, isAfterDate)

        template.updateMulti(Query.query(criteria), update, Standup::class.java)
    }


    /**
     * Creates [Criteria] object of given values
     */
    private fun criteriaOf(withIds: Set<String>? = null,
                           withStandupDefinitionId: String? = null,
                           withStatus: Set<Standup.Status>? = null,
                           withoutStatus: Set<Standup.Status>? = null,
                           isOnDate: LocalDate? = null,
                           isBeforeDate: LocalDate? = null,
                           isOnTime: LocalTime? = null,
                           isBeforeOrOnTime: LocalTime? = null,
                           withUserIds: Set<String>? = null,
                           withBroadcastChannelId: String? = null,
                           hasNumberOfAskedQuestions: Int? = null,
                           timestampIsBefore: Instant? = null,
                           timestampIsAfter: Instant? = null,
                           hasAnswer: Standup.Answer? = null,
                           isAfterDate: LocalDate? = null,
                           isInDates: List<String>? = null
    ): Criteria {
        val criteriaList = mutableListOf<Criteria>()

        withIds?.let { criteriaList.add(where("_id").`in`(it)) }

        withStandupDefinitionId?.let { criteriaList.add(where(Standup.STANDUP_DEFINITION_ID).`is`(it)) }

        withStatus?.let { criteriaList.add(where(Standup.STATUS).`in`(it)) }

        withoutStatus?.let { criteriaList.add(where(Standup.STATUS).nin(it)) }

        isOnDate?.let { criteriaList.add(where(Standup.DATE).`is`(it)) }

        isBeforeDate?.let { criteriaList.add(where(Standup.DATE).lt(it)) }

        isAfterDate?.let { criteriaList.add(where(Standup.DATE).gt(it)) }

        isOnTime?.let { criteriaList.add(where(Standup.TIME).`is`(it)) }

        isBeforeOrOnTime?.let { criteriaList.add(where(Standup.TIME).lte(it)) }

        withUserIds?.let { criteriaList.add(where(Standup.USER_ID).`in`(it)) }

        withBroadcastChannelId?.let { criteriaList.add(where(Standup.BROADCAST_CHANNEL_ID).`is`(it)) }

        timestampIsBefore?.let { criteriaList.add(where(Standup.TIMESTAMP).lt(it)) }

        timestampIsAfter?.let { criteriaList.add(where(Standup.TIMESTAMP).gt(it)) }

        hasNumberOfAskedQuestions?.let { criteriaList.add(where(Standup.QUESTIONS_ASKED).`is`(it)) }

        hasAnswer?.let { criteriaList.add(where(Standup.ANSWERS).`in`(it)) }

        isInDates?.let { criteriaList.add(where(Standup.DATE).`in`(it)) }

        return if (criteriaList.isEmpty()) Criteria() else Criteria().andOperator(*criteriaList.toTypedArray())
    }


    @Cacheable(cacheNames = [Standup.ANSWERED_COLLECTION], sync = true)
    override fun getAnsweredStandups(): String {
        val criteria = Criteria()
        criteria.and(Standup.QUESTIONS_ASKED).ne(0)
        criteria.and(Standup.STATUS).`is`(Standup.Status.CLOSED)
        val agg = newAggregation(Standup::class.java,
                match(criteria),
                project()
                        .and("answers")
                        .size()
                        .`as`("count"))
        val results = template.aggregate(
                agg, ArrayCount::class.java
        )
        return results.mappedResults.map { it.count }.sum().toString()
    }

    override fun existsByCriteria(criteria: Criteria): Boolean {
        return this.template.exists(Query.query(criteria), Standup::class.java)
    }

    override fun findStandups(query: Query): List<Standup> {
        return this.template.find(query = query)
    }

    override fun updateStandup(query: Query, update: Update): Standup? {
        return this.template.findAndModify(query, update, Standup::class.java)
    }

    override fun updateStandups(query: Query, update: Update): UpdateResult {
        return this.template.updateMulti(query, update, Standup::class.java)
    }

}
