package com.kreait.bots.agile.domain.common.actuator

import com.kreait.bots.agile.domain.common.data.SlackTeamRepository
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.stereotype.Component

@Component
@Endpoint(id = "customers")
class CustomerMetricsEndpoint @Autowired constructor(private val slackTeamRepository: SlackTeamRepository,
                                                     private val standupDefinitionRepository: StandupDefinitionRepository,
                                                     private val standupRepository: StandupRepository) {


    @ReadOperation
    open fun getMetrics(): Metrics {
        val slackteams = slackTeamRepository.countTeams()
        val members = standupDefinitionRepository.getMemberCount()
        val questions = standupRepository.getAnsweredStandups()
        val standups = standupDefinitionRepository.getActiveCount()
        return Metrics(slackteams, members, questions, standups)
    }
}
