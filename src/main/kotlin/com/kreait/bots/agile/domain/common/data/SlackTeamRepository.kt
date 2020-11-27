package com.kreait.bots.agile.domain.common.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service


@Service
class SlackTeamRepository constructor(@Autowired private val template: MongoTemplate) {
    fun archiveSlackTeam(teamId: String) {
        val team = template.findById(teamId, SlackTeam::class.java)
        team!!.status = SlackTeam.Status.ARCHIVED
        template.save(team)
    }

    @Cacheable(cacheNames = ["SlackTeam.count"])
    fun countTeams(): String {
        return template.count(Query.query(Criteria.where("status").`is`(SlackTeam.Status.ACTIVE)), SlackTeam::class.java).toString()
    }

    fun find(id: String): SlackTeam? {
        return template.findById(id, SlackTeam::class.java)
    }

    fun save(slackTeam: SlackTeam) {
        template.save(slackTeam)
    }
}
