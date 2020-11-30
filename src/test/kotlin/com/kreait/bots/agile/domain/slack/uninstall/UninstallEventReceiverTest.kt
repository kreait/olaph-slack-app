package com.kreait.bots.agile.domain.slack.uninstall

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.SlackTeam
import com.kreait.bots.agile.domain.common.data.SlackTeamRepository
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.SlackEventTest
import com.nhaarman.mockitokotlin2.mock
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import com.kreait.slack.api.contract.jackson.event.sample
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant

@SpringBootTest(classes = [TestApplication::class])
@IntegrationTest
class UninstallEventReceiverTest @Autowired constructor(private val slackTeamRepository: SlackTeamRepository,
                                                        private val standupDefinitionRepository: StandupDefinitionRepository,
                                                        private val standupRepository: StandupRepository) : SlackEventTest {

    @BeforeEach
    fun setup() {
        standupRepository.deleteAll()
        standupDefinitionRepository.deleteAll()
    }

    @DisplayName("Test Uninstallation Receiver")
    @Test
    override fun onReceiveEvent() {
        slackTeamRepository.save(SlackTeam("teamId", "name",
                SlackTeam.IncomingWebhook("", "", "", ""),
                SlackTeam.Bot("", ""),
                SlackTeam.Status.ACTIVE,
                Instant.now()))
        standupDefinitionRepository.insert(StandupDefinition.sample().copy(teamId = "teamId", id = "standupDefinition"))
        standupRepository.insert(Standup.sample().copy(teamId = "teamId"))
        val uninstallEventReceiver = UninstallEventReceiver(standupDefinitionRepository, standupRepository, slackTeamRepository)
        uninstallEventReceiver.onReceiveEvent(SlackEvent.sample().copy(event = mapOf(Pair("type", "app_uninstalled")), teamId = "teamId"), HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        Assertions.assertEquals(standupDefinitionRepository.findAllActive("teamId").size, 0)
        Assertions.assertEquals(slackTeamRepository.find("teamId")!!.status, SlackTeam.Status.ARCHIVED)
        Assertions.assertEquals(standupRepository.find(withStandupDefinitionId = "standupDefinition").size, 0)

    }

    @DisplayName("Test supports Uninstallation")
    @Test
    override fun supportsEvent() {
        val uninstallEventReceiver = UninstallEventReceiver(mock(), mock(), mock())
        uninstallEventReceiver.supportsEvent(SlackEvent.sample().copy(event = mapOf(Pair("type", "app_uninstalled"))))
    }
}
