package com.kreait.bots.agile.core.standup.create

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate

@IntegrationTest
@SpringBootTest(classes = [TestApplication::class])
class StandupCreationServiceTests @Autowired constructor(private val standupRepository: StandupRepository,
                                                         private val standupDefinitionRepository: StandupDefinitionRepository) {

    @DisplayName("Test Standup Creation Service")
    @Test
    fun testStandupCreationService() {
        val standupdefinition = standupDefinitionRepository.insert(
                StandupDefinition.sample().copy(status = StandupDefinition.Status.ACTIVE,
                        days = listOf(LocalDate.now().dayOfWeek),
                        subscribedUserIds = listOf("user1")))
        val standupCreationService = StandupCreationService(standupRepository, standupDefinitionRepository)
        standupCreationService.createStandupsFromStandupDefinitions()
        Assertions.assertEquals(standupRepository.find(withStandupDefinitionId = standupdefinition.id).size, 1)
    }
}