package com.kreait.bots.agile.domain.slack.uninstall

import com.kreait.bots.agile.domain.common.data.SlackTeamRepository
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import com.kreait.slack.broker.receiver.EventReceiver
import com.kreait.slack.broker.store.team.Team
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class UninstallEventReceiver @Autowired constructor(private val standupDefinitionRepository: StandupDefinitionRepository,
                                                    private val standupRepository: StandupRepository,
                                                    private val slackTeamRepository: SlackTeamRepository) : EventReceiver, MeterBinder {
    private var counter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.uninstallations")
                .description("Total number of olaph - uninstallations")
                .register(registry)
    }


    companion object {
        val LOG = LoggerFactory.getLogger(UninstallEventReceiver::class.java)
    }

    override fun supportsEvent(slackEvent: SlackEvent): Boolean {
        return slackEvent.event["type"] == "app_uninstalled"
    }

    override fun onReceiveEvent(slackEvent: SlackEvent, headers: HttpHeaders, team: Team) {
        LOG.info("Uninstalled: $slackEvent")
        counter?.increment()
        this.slackTeamRepository.archiveSlackTeam(slackEvent.teamId)


        val teamStandupIds = this.archiveStandupDefinitions(slackEvent.teamId)

        this.closeTeamStandups(teamStandupIds)
    }

    /**
     * Archives all [StandupDefinition]s for given team and returns ids of the standup
     */
    private fun archiveStandupDefinitions(teamId: String): Set<String> {
        return this.standupDefinitionRepository.find(withTeamId = teamId).map {
            this.standupDefinitionRepository.save(it.copy(status = StandupDefinition.Status.ARCHIVED)).id!!
        }.toSet()
    }

    private fun closeTeamStandups(teamStandupIds: Set<String>) {
        this.standupRepository.update(withIds = teamStandupIds, update = Update.update(Standup.STATUS, Standup.Status.CLOSED))
    }
}
