package com.kreait.bots.agile.core.standup

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.common.example
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.Standup.Status
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
@IntegrationTest
internal class StandupServiceTest constructor(@Autowired val standupRepository: StandupRepository) {

    @BeforeEach
    fun setUp() {
        this.standupRepository.deleteAll()
    }

    @Nested
    inner class TestExists {

        private val userId1 = "userId1"

        @DisplayName("exists Standup.Answer for given criteria")
        @Test
        fun testExistsWith() {
            val answer = Standup.Answer.example()
            val standup = standupRepository.insert(Standup.example(status = Status.OPEN, userId = userId1, answers = listOf(answer)))
            standupRepository.insert(Standup.example(status = Status.CREATED))
            standupRepository.insert(Standup.example(date = LocalDate.of(2000, 12, 24)))

            Assertions.assertTrue(standupRepository.exists(withUserIds = setOf(userId1), hasAnswer = answer))
            Assertions.assertTrue(standupRepository.exists(withStandupDefinitionId = standup.standupDefinitionId))
            Assertions.assertTrue(standupRepository.exists(withStatus = setOf(standup.status)))
        }

    }


    @Nested
    inner class TestFind {

        private val userId1 = "userId1"
        private val userId2 = "userId2"

        @DisplayName("find Standups for given criteria")
        @Test
        fun testFindWith() {
            val answer = Standup.Answer.example()
            val standup = standupRepository.insert(Standup.example(status = Status.OPEN, userId = userId1, answers = listOf(answer)))
            standupRepository.insert(Standup.example(userId = userId2, answers = listOf(answer)))
            standupRepository.insert(Standup.example(status = Status.CREATED))
            standupRepository.insert(Standup.example(date = LocalDate.of(2000, 12, 24)))

            Assertions.assertEquals(1, standupRepository.find(withUserIds = setOf(userId1), hasAnswer = standup.answers[0]).size)
            Assertions.assertEquals(1, standupRepository.find(withStatus = setOf(standup.status)).size)
            Assertions.assertEquals(1, standupRepository.find(isBeforeDate = LocalDate.of(2001, 12, 24)).size)
        }

    }

    @Nested
    inner class TestBasicQuery {

        @Test
        fun testBroadcastStandupsCorrect() {
            val standup1 = standupRepository.insert(Standup.example(status = Status.OPEN, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1", "2")))
            val standup2 = standupRepository.insert(Standup.example(status = Status.OPEN, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1", "2")))
            standupRepository.insert(Standup.example(status = Status.OPEN, answers = listOf(Standup.Answer.example()), questions = listOf("1", "2")))

            val standupsToBroadcast = standupRepository.findAnsweredAndOpen()

            Assertions.assertEquals(2, standupsToBroadcast.size)
            Assertions.assertTrue(standupsToBroadcast.asSequence().map { it.id }.contains(standup1.id))
            Assertions.assertTrue(standupsToBroadcast.asSequence().map { it.id }.contains(standup2.id))
        }

        @Test
        fun testBroadcastStandupsWrongStatus() {
            standupRepository.insert(Standup.example(status = Status.CANCELLED, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1", "2")))
            standupRepository.insert(Standup.example(status = Status.CREATED, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1", "2")))
            standupRepository.insert(Standup.example(status = Status.OPEN, answers = listOf(Standup.Answer.example()), questions = listOf("1", "2")))

            Assertions.assertEquals(0, standupRepository.findAnsweredAndOpen().size)
        }

        @Test
        fun testBroadcastStandupsWrong() {
            standupRepository.insert(Standup.example(status = Status.CANCELLED, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1")))
            standupRepository.insert(Standup.example(status = Status.CREATED, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1")))
            standupRepository.insert(Standup.example(status = Status.OPEN, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1")))

            Assertions.assertEquals(0, standupRepository.findAnsweredAndOpen().size)
        }

        @Test
        fun testBroadcastStandupsUserId() {
            val standup1 = standupRepository.insert(Standup.example(userId = "1", status = Status.OPEN, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1", "2")))
            standupRepository.insert(Standup.example(status = Status.OPEN, answers = listOf(Standup.Answer.example(), Standup.Answer.example()), questions = listOf("1", "2")))
            standupRepository.insert(Standup.example(status = Status.OPEN, answers = listOf(Standup.Answer.example()), questions = listOf("1", "2")))

            val standupsToBroadcast = standupRepository.findAnsweredAndOpen(userId = "1")

            Assertions.assertEquals(1, standupsToBroadcast.size)
            Assertions.assertTrue(standupsToBroadcast.asSequence().map { it.id }.contains(standup1.id))
        }
    }
}
