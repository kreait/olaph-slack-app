package com.kreait.bots.agile.core.configuration

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.kreait.bots.agile.domain.common.data.SlackTeam
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.broker.store.team.TeamNotFoundException
import com.kreait.slack.broker.store.team.TeamStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class MongoTeamStore @Autowired constructor(private val template: MongoTemplate) : TeamStore {
    private var cache: Cache<String, Team> = Caffeine.newBuilder()
            .maximumSize(2000).build()

    override fun findById(id: String): Team {
        val cachedTeam = cache.getIfPresent(id)
        return if (cachedTeam == null) {
            val slackTeam = template.findById(id, SlackTeam::class.java)
                    ?: throw TeamNotFoundException("team $id not found")
            val team = Team(slackTeam.teamId, slackTeam.teamName,
                    Team.IncomingWebhook(slackTeam.incomingWebhook.channel,
                            slackTeam.incomingWebhook.channelId,
                            slackTeam.incomingWebhook.configurationUrl,
                            slackTeam.incomingWebhook.url),
                    Team.Bot(slackTeam.bot.userId,
                            slackTeam.bot.accessToken))
            cache.put(slackTeam.teamId, team)
            team
        } else cachedTeam
    }

    override fun put(team: Team) {
        template.save(SlackTeam(team.teamId,
                team.teamName,
                SlackTeam.IncomingWebhook(
                        team.incomingWebhook!!.channel,
                        team.incomingWebhook!!.channelId,
                        team.incomingWebhook!!.configurationUrl,
                        team.incomingWebhook!!.url
                ),
                SlackTeam.Bot(
                        team.bot.userId,
                        team.bot.accessToken
                ),
                SlackTeam.Status.ACTIVE,
                Instant.now()))
        cache.put(team.teamId, team)
    }

    override fun removeById(id: String) {
        val team = template.findById(id, SlackTeam::class.java)
        team!!.status = SlackTeam.Status.ARCHIVED
        template.save(team)
        cache.invalidate(id)
    }

}
