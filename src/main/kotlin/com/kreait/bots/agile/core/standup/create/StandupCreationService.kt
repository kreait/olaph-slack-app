package com.kreait.bots.agile.core.standup.create

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class StandupCreationService @Autowired constructor(private val standupRepository: StandupRepository,
                                                    private val standupDefinitionRepository: StandupDefinitionRepository) {

    companion object {
        private val LOG = LogFactory.getLog(StandupCreationService::class.java)!!
    }

    /**
     * Lifecycle part 1 "Standup gets created from StandupDefinition" as seen in WIKI
     */
    fun createStandupsFromStandupDefinitions() {
        this.standupDefinitionRepository
                .findAcrossAllWorkspaces(standupDays = listOf(LocalDate.now().dayOfWeek), status = setOf(StandupDefinition.Status.ACTIVE))
                .forEach { standupDefinition ->
                    createStandupsForStandupDefinition(standupDefinition)
                }
    }

    /**
     * Lifecycle part 1 "Standup gets created from StandupDefinition" as seen in WIKI
     */
    fun createStandupsForStandupDefinition(standupDefinition: StandupDefinition) {
        try {
            val standups = this.standupRepository.find(withStandupDefinitionId = standupDefinition.id, isOnDate = LocalDate.now())
            if (standups.size != standupDefinition.subscribedUserIds.size) {
                standupDefinition.subscribedUserIds.forEach { userId ->
                    if (standups.none { standup -> standup.userId == userId }) {
                        this.standupRepository.insert(Standup.of(standupDefinition = standupDefinition, userId = userId))
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error("Error during standup creation for $standupDefinition", e)
        }
    }

    /**
     * Lifecycle part 1 "Standup gets created from StandupDefinition" as seen in WIKI
     */
    fun createStandupsForStandupDefinition(standupDefinitionId: String) {
        val standupDefinition = this.standupDefinitionRepository.findById(standupDefinitionId)
        if (standupDefinition.isPresent) {
            this.createStandupsForStandupDefinition(standupDefinition.get())
        } else {
            LOG.error("Could not find standupdefinition with id $ standupDefinitionId")
        }

    }
}
