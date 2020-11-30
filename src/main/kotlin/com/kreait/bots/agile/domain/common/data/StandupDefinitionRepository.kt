package com.kreait.bots.agile.domain.common.data

import com.kreait.bots.agile.domain.common.actuator.ArrayCount
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.DayOfWeek
import java.time.LocalTime


interface StandupDefinitionRepository : MongoRepository<StandupDefinition, String>, StandupDefinitionRepositoryCustom

interface StandupDefinitionRepositoryCustom {

    fun find(
        withName: String? = null,
        withDays: List<DayOfWeek>? = null,
        withTime: LocalTime? = null,
        withBroadcastChannels: Set<String>? = null,
        withQuestions: List<String>? = null,
        withSubscribedUserIds: List<String>? = null,
        withoutSubcribedUserIds: Set<String>? = null,
        withStatus: Set<StandupDefinition.Status>? = null,
        withoutStatus: Set<StandupDefinition.Status>? = null,
        withTeamId: String,
        withUserId: String? = null,
        withId: String? = null
    )
            : List<StandupDefinition>

    fun update(
        withName: String? = null,
        withDays: List<DayOfWeek>? = null,
        withTime: LocalTime? = null,
        withBroadcastChannels: Set<String>? = null,
        withQuestions: List<String>? = null,
        withSubscribedUserIds: List<String>? = null,
        withoutSubcribedUserIds: Set<String>? = null,
        withStatus: Set<StandupDefinition.Status>? = null,
        withoutStatus: Set<StandupDefinition.Status>? = null,
        withTeamId: String? = null,
        withUserId: String? = null,
        withId: String? = null,
        update: Update
    )

    fun findAllActive(teamId: String): List<StandupDefinition>

    fun findById(id: String?, teamId: String, status: Set<StandupDefinition.Status>? = null): StandupDefinition

    fun existsById(standupDefinitionId: String, teamId: String): Boolean
    fun findAcrossAllWorkspaces(
        standupName: String? = null, standupDays: List<DayOfWeek>? = null, standupTime: LocalTime? = null,
        standupBroadcastChannelId: Set<String>? = null, standupQuestions: List<String>? = null,
        standupSubscribedUserIds: List<String>? = null,
        status: Set<StandupDefinition.Status>? = null
    ): List<StandupDefinition>

    fun getMemberCount(): String
    fun getActiveCount(): String

    fun addUserId(standupDefinitionId: String, userId: String)
}

open class StandupDefinitionRepositoryImpl constructor(@Autowired private val template: MongoTemplate) :
    StandupDefinitionRepositoryCustom {
    override fun update(
        withName: String?, withDays: List<DayOfWeek>?, withTime: LocalTime?,
        withBroadcastChannels: Set<String>?, withQuestions: List<String>?,
        withSubscribedUserIds: List<String>?, withoutSubcribedUserIds: Set<String>?,
        withStatus: Set<StandupDefinition.Status>?, withoutStatus: Set<StandupDefinition.Status>?,
        withTeamId: String?, withUserId: String?, withId: String?, update: Update
    ) {

        val criteria = criteriaOf(
            standupName = withName,
            standupDays = withDays,
            standupTime = withTime,
            standupBroadcastChannelIds = withBroadcastChannels,
            standupQuestions = withQuestions,
            standupSubscribedUserIds = withSubscribedUserIds,
            notInSubscribedUserIds = withoutSubcribedUserIds,
            hasStatus = withStatus,
            doestNotHaveStatus = withoutStatus,
            teamId = withTeamId,
            userId = withUserId,
            id = withId
        )
        template.updateMulti(Query.query(criteria), update, StandupDefinition::class.java)
    }

    override fun addUserId(standupDefinitionId: String, userId: String) {
        val standupById = Query.query(Criteria.where("_id").`is`(standupDefinitionId))
        val addUserIdToList = Update().push(StandupDefinition.SUBSCRIBED_USER_IDS).value(userId)
        template.updateFirst(standupById, addUserIdToList, StandupDefinition::class.java)
    }

    @Cacheable(cacheNames = [StandupDefinition.STANDUP_DEFINITION_COUNT], sync = true)
    override fun getActiveCount(): String {
        val criteria = criteriaOf(hasStatus = setOf(StandupDefinition.Status.ACTIVE))
        return template.count(Query.query(criteria), StandupDefinition::class.java).toString()
    }

    @Cacheable(cacheNames = [StandupDefinition.MEMBER_COLLECTION], sync = true)
    override fun getMemberCount(): String {
        val criteria = Criteria()
        criteria.and(StandupDefinition.STATUS).`is`(StandupDefinition.Status.ACTIVE)
        val agg = Aggregation.newAggregation(
            StandupDefinition::class.java,
            Aggregation.match(criteria),
            Aggregation.project()
                .and("subscribedUserIds")
                .size()
                .`as`("count")
        )
        val results = template.aggregate(agg, ArrayCount::class.java)
        return results.mappedResults.map { it.count }.sum().toString()
    }

    override fun existsById(standupDefinitionId: String, teamId: String): Boolean {
        val criteria =
            criteriaOf(teamId = teamId, id = standupDefinitionId, hasStatus = setOf(StandupDefinition.Status.ACTIVE))
        return !template.find(Query.query(criteria), StandupDefinition::class.java).isEmpty()
    }

    override fun findAcrossAllWorkspaces(
        standupName: String?, standupDays: List<DayOfWeek>?, standupTime: LocalTime?,
        standupBroadcastChannelId: Set<String>?, standupQuestions: List<String>?,
        standupSubscribedUserIds: List<String>?,
        status: Set<StandupDefinition.Status>?
    ): List<StandupDefinition> {

        val criteria = criteriaOf(
            standupName = standupName, standupDays = standupDays, standupTime = standupTime,
            hasStatus = status, standupBroadcastChannelIds = standupBroadcastChannelId,
            standupSubscribedUserIds = standupSubscribedUserIds, standupQuestions = standupQuestions
        )
        return template.find(Query.query(criteria), StandupDefinition::class.java)
    }

    override fun findById(id: String?, teamId: String, status: Set<StandupDefinition.Status>?): StandupDefinition {
        val criteria = criteriaOf(teamId = teamId, id = id, hasStatus = status)

        return template.find(Query.query(criteria), StandupDefinition::class.java).first()
    }

    override fun findAllActive(teamId: String): List<StandupDefinition> {
        val criteria = criteriaOf(teamId = teamId, hasStatus = setOf(StandupDefinition.Status.ACTIVE))
        return template.find(Query.query(criteria), StandupDefinition::class.java)
    }

    override fun find(
        withName: String?, withDays: List<DayOfWeek>?, withTime: LocalTime?,
        withBroadcastChannels: Set<String>?, withQuestions: List<String>?,
        withSubscribedUserIds: List<String>?, withoutSubcribedUserIds: Set<String>?,
        withStatus: Set<StandupDefinition.Status>?, withoutStatus: Set<StandupDefinition.Status>?,
        withTeamId: String, withUserId: String?, withId: String?
    ): List<StandupDefinition> {

        val criteria = criteriaOf(
            standupName = withName,
            standupDays = withDays,
            standupTime = withTime,
            standupBroadcastChannelIds = withBroadcastChannels,
            standupQuestions = withQuestions,
            standupSubscribedUserIds = withSubscribedUserIds,
            notInSubscribedUserIds = withoutSubcribedUserIds,
            hasStatus = withStatus,
            doestNotHaveStatus = withoutStatus,
            teamId = withTeamId,
            userId = withUserId
        )

        return template.find(Query.query(criteria), StandupDefinition::class.java)
    }

    private fun criteriaOf(
        standupName: String? = null,
        standupDays: List<DayOfWeek>? = null,
        standupTime: LocalTime? = null,
        standupBroadcastChannelIds: Set<String>? = null,
        standupQuestions: List<String>? = null,
        standupSubscribedUserIds: List<String>? = null,
        notInSubscribedUserIds: Set<String>? = null,
        hasStatus: Set<StandupDefinition.Status>? = null,
        doestNotHaveStatus: Set<StandupDefinition.Status>? = null,
        teamId: String? = null,
        userId: String? = null,
        id: String? = null
    ): Criteria {
        val criteriaList = mutableListOf<Criteria>()

        standupName?.let { criteriaList.add(where(StandupDefinition.NAME).`is`(it)) }

        standupDays?.let { criteriaList.add(where(StandupDefinition.DAYS).all(it)) }

        standupTime?.let { criteriaList.add(where(StandupDefinition.TIME).`is`(it)) }

        standupBroadcastChannelIds?.let { criteriaList.add(where(StandupDefinition.BROADCAST_CHANNEL_ID).`in`(it)) }

        standupSubscribedUserIds?.let { criteriaList.add(where(StandupDefinition.SUBSCRIBED_USER_IDS).`in`(it)) }

        notInSubscribedUserIds?.let { criteriaList.add(where(StandupDefinition.SUBSCRIBED_USER_IDS).nin(it)) }

        standupQuestions?.let { criteriaList.add(where(StandupDefinition.QUESTIONS).`is`(it)) }

        hasStatus?.let { criteriaList.add(where(StandupDefinition.STATUS).`in`(it)) }

        doestNotHaveStatus?.let { criteriaList.add(where(StandupDefinition.STATUS).nin(it)) }

        userId?.let { criteriaList.add(where(StandupDefinition.SUBSCRIBED_USER_IDS).`in`(it)) }

        id?.let { criteriaList.add(where("_id").`is`(ObjectId(it))) }

        teamId?.let { criteriaList.add(where(StandupDefinition.TEAM_ID).`is`(it)) }

        return if (criteriaList.isEmpty()) Criteria() else Criteria().andOperator(*criteriaList.toTypedArray())
    }


}
