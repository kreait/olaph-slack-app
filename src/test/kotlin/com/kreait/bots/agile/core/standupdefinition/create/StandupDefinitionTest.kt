package com.kreait.bots.agile.core.standupdefinition.create

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.query.Update
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.DayOfWeek
import java.time.LocalTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
@DisplayName("TestStandupDefinition - TeamId consistency")
class StandupDefinitionTest constructor(@Autowired val standupDefinitionRepository: StandupDefinitionRepository,
                                        @Autowired val standupRepository: StandupRepository) {

    /**
     * tests if findAllActive-Method really retrieves only the StandupDefinitions with according teamId
     */
    @Test
    @DisplayName("Team ID")
    fun testTeamId() {
        standupDefinitionRepository.deleteAll()
        standupDefinitionRepository.save(StandupDefinition(name = "CorrectWorkspace", days = listOf(DayOfWeek.WEDNESDAY), time = LocalTime.NOON,
                broadcastChannelId = "CHANNEL", questions = listOf("what"), teamId = "TEAM1", subscribedUserIds = listOf("USER1")))
        standupDefinitionRepository.save(StandupDefinition(name = "WrongWorkspace", days = listOf(DayOfWeek.WEDNESDAY), time = LocalTime.NOON,
                broadcastChannelId = "CHANNEL", questions = listOf("what"), teamId = "TEAM2", subscribedUserIds = listOf("USER1")))
        Assertions.assertEquals(standupDefinitionRepository.findAllActive("TEAM1").size, 1)
        Assertions.assertEquals(standupDefinitionRepository.findAllActive("TEAM2").size, 1)
        Assertions.assertEquals(standupDefinitionRepository.findAllActive("TEAM3").size, 0)
    }

    /**
     * tests if archiveStandup-Method is working correctly
     */
    @Test
    @DisplayName("Archiving Standup")
    fun archiveStandup() {
        standupDefinitionRepository.deleteAll()
        val first = standupDefinitionRepository.save(StandupDefinition(name = "FirstStandup", days = listOf(DayOfWeek.WEDNESDAY), time = LocalTime.NOON,
                broadcastChannelId = "CHANNEL", questions = listOf("what"), teamId = "TEAM1", subscribedUserIds = listOf("USER1")))
        Assertions.assertEquals(standupDefinitionRepository.findAllActive("TEAM1").size, 1)
        deleteStandup(first.id)
        Assertions.assertEquals(standupDefinitionRepository.findAllActive("TEAM1").size, 0)
        Assertions.assertEquals(standupDefinitionRepository.findById(first.id!!).get().status, StandupDefinition.Status.ARCHIVED)

        val second = standupDefinitionRepository.save(StandupDefinition(name = "SecondStandup", days = listOf(DayOfWeek.WEDNESDAY), time = LocalTime.NOON,
                broadcastChannelId = "CHANNEL", questions = listOf("second"), teamId = "TEAM1", subscribedUserIds = listOf("USER1")))
        Assertions.assertEquals(standupDefinitionRepository.findAllActive("TEAM1").size, 1)
        deleteStandup(second.id)
        Assertions.assertEquals(standupDefinitionRepository.findAllActive("TEAM1").size, 0)
        Assertions.assertEquals(standupDefinitionRepository.findById(second.id!!).get().status, StandupDefinition.Status.ARCHIVED)

    }

    private fun deleteStandup(id: String?) {
        this.standupDefinitionRepository.update(withId = id, withStatus = setOf(StandupDefinition.Status.ACTIVE),
                update = Update().set(StandupDefinition.STATUS, StandupDefinition.Status.ARCHIVED))
        this.standupRepository.update(withoutStatus = setOf(Standup.Status.CLOSED), withStandupDefinitionId = id,
                update = Update().set(Standup.STATUS, Standup.Status.CANCELLED))

    }
}
