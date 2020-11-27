package com.kreait.bots.agile.core.standup.open

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.common.example
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.Standup.Status
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.standup.SlackOpeningMessageSender
import com.kreait.bots.agile.domain.slack.standup.Successful
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
internal class StandupOpeningServiceTest @Autowired constructor(private val standupRepository: StandupRepository) {

    private val predicates = Standup.Predicates(this.standupRepository)

    @BeforeEach
    fun setUp() {
        this.standupRepository.deleteAll()
    }

    @Test
    fun testTransitionStandupStatusToOpenWrong() {
        val standup = standupRepository.insert(Standup.example())
        standupRepository.insert(Standup.example())
        standupRepository.insert(Standup.example(userId = "userId2"))

        Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup))
    }

    @Test
    fun testTransitionStandupStatusToOpenCorrect() {
        val standup = standupRepository.insert(Standup.example())
        standupRepository.insert(Standup.example())
        standupRepository.insert(Standup.example(status = Status.OPEN, userId = "userId2"))

        Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup))
    }

    @Test
    fun testOpeningQuery() {
        val standup = standupRepository.insert(Standup.example())
        standupRepository.insert(Standup.example())
        val falseStandup = standupRepository.insert(Standup.example(status = Status.OPEN, userId = "userId2"))

        val standups = this.standupRepository.find(withStatus = setOf(Status.CREATED), timestampIsAfter = Instant.now().minus(Duration.ofDays(1)))
                .groupBy { it.userId } // group by user
                .map { it.value.first() } // pick the first standup per user
                .filter { Standup.Predicates.canTransitionToOpen(it) }
        Assertions.assertFalse(standups.contains(falseStandup))
        Assertions.assertTrue(standups.contains(standup))
    }

    @Test
    fun testQuery() {
        val wrongStandup = standupRepository.insert(Standup.example(userId = "User1"))
        val wrongStandup2 = standupRepository.insert(Standup.example(userId = "User1", status = Status.OPEN))
        val correctStandup = standupRepository.insert(Standup.example(userId = "User2"))
        val correctStandup2 = standupRepository.insert(Standup.example(userId = "User2"))

        val criteria = Criteria()

        criteria.and(Standup.USER_ID).nin(standupRepository.find(withStatus = setOf(Status.OPEN), isOnDate = LocalDate.now()).map { standup -> standup.userId })
        val standups = standupRepository.findStandups(Query.query(criteria))
        Assertions.assertFalse(standups.contains(wrongStandup))
        Assertions.assertFalse(standups.contains(wrongStandup2))

        Assertions.assertTrue(standups.contains(correctStandup))
        Assertions.assertTrue(standups.contains(correctStandup2))
    }

    @DisplayName("test opening Service")
    @Test
    fun testOpeningService() {
        standupRepository.insert(Standup.sample())
        standupRepository.insert(Standup.sample())
        val openingMessageSender = mock<SlackOpeningMessageSender> {
            on { sendOpeningMessage(any()) } doReturn (Successful(true, ""))
        }
        val openingService = StandupOpeningService(mock(), openingMessageSender, standupRepository, mock())
        openingService.findAndOpenStandups()
        verify(openingMessageSender, times(1)).sendOpeningMessage(any())
    }

    @DisplayName("test false Service")
    @Test
    fun testFalseOpeningService() {
        val standup = standupRepository.insert(Standup.sample())
        standupRepository.insert(Standup.sample())
        val openingMessageSender = mock<SlackOpeningMessageSender> {
            on { sendOpeningMessage(any()) } doReturn (Successful(false, "account_inactive"))
        }
        val openingService = StandupOpeningService(mock(), openingMessageSender, standupRepository, mock())
        openingService.findAndOpenStandups()
        Assertions.assertEquals(standupRepository.findById(standup.id!!).get().status, Status.CANCELLED)
    }

    @DisplayName("test open single standup Service")
    @Test
    fun testOpenSingleService() {
        standupRepository.insert(Standup.sample().copy(userId = "sampleUser", teamId = "sampleTeam"))
        standupRepository.insert(Standup.sample())
        val openingMessageSender = mock<SlackOpeningMessageSender> {
            on { sendOpeningMessage(any()) } doReturn (Successful(true, ""))
        }
        val openingService = StandupOpeningService(mock(), openingMessageSender, standupRepository, mock())
        openingService.openStandup("sampleUser", "sampleTeam")
        verify(openingMessageSender, times(1)).sendOpeningMessage(any())
    }


}
