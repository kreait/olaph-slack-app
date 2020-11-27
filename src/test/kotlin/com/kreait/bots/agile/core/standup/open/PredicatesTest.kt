package com.kreait.bots.agile.core.standup.open

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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
internal class PredicatesTest @Autowired constructor(private val standupRepository: StandupRepository) {

    private val predicates = Standup.Predicates(this.standupRepository)
    private val userId2 = "userId2"

    @BeforeEach
    fun setUp() {
        this.standupRepository.deleteAll()
    }

    @Nested
    inner class TestStatus {

        @DisplayName("test with wrong Status")
        @ParameterizedTest
        @EnumSource(value = Status::class, names = ["OPEN", "CLOSED", "CANCELLED"])
        fun testWrongStatus(status: Status) {
            val standup = standupRepository.insert(Standup.example(status = status))
            Assertions.assertFalse(Standup.Predicates.canTransitionToOpen(standup))
        }

        @DisplayName("test with correct Status")
        @ParameterizedTest
        @EnumSource(value = Status::class, names = ["CREATED"])
        fun testCorrectStatus(status: Status) {
            val standup = standupRepository.insert(Standup.example(status = status))
            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup))
        }
    }

    @Nested
    inner class TestDate {

        @DisplayName("test with future date")
        @Test
        fun testWrongDate() {
            val standup = standupRepository.insert(Standup.example(date = LocalDate.now().plusDays(1)))
            Assertions.assertFalse(Standup.Predicates.canTransitionToOpen(standup))
        }

        @DisplayName("test with current or past date")
        @Test
        fun testCorrectDate() {
            val standup1 = standupRepository.insert(Standup.example())
            val standup2 = standupRepository.insert(Standup.example(date = LocalDate.now().minusDays(1)))

            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup1))
            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup2))

        }
    }

    @Nested
    inner class TestTime {

        @DisplayName("test with future time")
        @Test
        fun testWrongTime() {
            val standup = standupRepository.insert(Standup.example(time = LocalTime.now().plusHours(1)))
            Assertions.assertFalse(Standup.Predicates.canTransitionToOpen(standup))
        }

        @DisplayName("test with current or past time")
        //@Test
        fun testCorrectTime() {
            val standup1 = standupRepository.insert(Standup.example())
            val standup2 = standupRepository.insert(Standup.example(time = LocalTime.now().minusHours(1)))

            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup1))
            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup2))

        }
    }

    @Nested
    inner class TestUserId {

        @DisplayName("test with other standup in status open for same userId")
        @Test
        fun testWrongUserId() {
            val standup = standupRepository.insert(Standup.example())
            standupRepository.insert(Standup.example(status = Status.OPEN))
            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup))
        }

        @DisplayName("test with multiple standups in status created per userId")
        @Test
        fun testCorrectUserId() {
            val standup1 = standupRepository.insert(Standup.example())
            val standup2 = standupRepository.insert(Standup.example())

            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup1))
            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup2))
        }

        @DisplayName("test with standup in status open with different userId")
        @Test
        fun testCorrectUserId2() {
            val standup1 = standupRepository.insert(Standup.example())
            standupRepository.insert(Standup.example(status = Status.OPEN, userId = userId2))

            Assertions.assertTrue(Standup.Predicates.canTransitionToOpen(standup1))
        }
    }

}
