package com.kreait.bots.agile.core.standup.data.repository

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.common.example
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.stream.IntStream

@DisplayName("StandupRepository Tests")
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StandupRepositoryTests(@Autowired private val standupRepository: StandupRepository) {


    @BeforeEach
    fun setup() {
        this.standupRepository.deleteAll()
    }

    @Nested
    @DisplayName("Find Method")
    inner class FindByTests {

        @Test
        @DisplayName("withIds")
        fun withIds() {

            val sampleStandup1 = standupRepository.insert(Standup.example())
            val sampleStandup2 = standupRepository.insert(Standup.example())

            val result = standupRepository.find(withIds = setOf(sampleStandup1.id!!, sampleStandup2.id!!))

            assertEquals(2, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertTrue(result.contains(sampleStandup2), "contains second element")
        }

        @Test
        @DisplayName("withStandupDefinitionId")
        fun withStandupDefinitionId() {
            val sampleId = UUID.randomUUID().toString()
            val sampleStandup1 = standupRepository.insert(Standup.example(standupDefinitionId = sampleId))
            val sampleStandup2 = standupRepository.insert(Standup.example())

            val result = standupRepository.find(withStandupDefinitionId = sampleId)

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("withStatus")
        fun withStatus() {

            val sampleStandup1 = standupRepository.insert(Standup.example(status = Standup.Status.CANCELLED))
            val sampleStandup2 = standupRepository.insert(Standup.example(status = Standup.Status.OPEN))

            val result = standupRepository.find(withStatus = setOf(Standup.Status.CANCELLED))

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("withoutStatus")
        fun withoutStatus() {

            val sampleStandup1 = standupRepository.insert(Standup.example(status = Standup.Status.CANCELLED))
            val sampleStandup2 = standupRepository.insert(Standup.example(status = Standup.Status.OPEN))

            val result = standupRepository.find(withoutStatus = setOf(Standup.Status.OPEN))

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("isOnDate")
        fun isOnDate() {

            val sampleStandup1 = standupRepository.insert(Standup.example(date = LocalDate.of(2019, 10, 2)))
            val sampleStandup2 = standupRepository.insert(Standup.example(date = LocalDate.of(2019, 10, 3)))

            val result = standupRepository.find(isOnDate = sampleStandup1.date)

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("isBeforeDate")
        fun isBeforeDate() {
            val sampleStandup1 = standupRepository.insert(Standup.example(date = LocalDate.of(2019, 10, 2)))
            val sampleStandup2 = standupRepository.insert(Standup.example(date = LocalDate.of(2019, 10, 3)))

            val result = standupRepository.find(isBeforeDate = LocalDate.of(2019, 10, 3))

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("isOnTime")
        fun isOnTime() {
            val sampleStandup1 = standupRepository.insert(Standup.example(time = LocalTime.of(10, 0)))
            val sampleStandup2 = standupRepository.insert(Standup.example(time = LocalTime.of(10, 1)))

            val result = standupRepository.find(isOnTime = LocalTime.of(10, 0))

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("isBeforeOrOnTime")
        fun isBeforeOrOnTime() {
            val sampleStandup1 = standupRepository.insert(Standup.example(time = LocalTime.of(10, 0)))
            val sampleStandup2 = standupRepository.insert(Standup.example(time = LocalTime.of(10, 1)))
            val sampleStandup3 = standupRepository.insert(Standup.example(time = LocalTime.of(10, 2)))

            val result = standupRepository.find(isBeforeOrOnTime = LocalTime.of(10, 1))

            assertEquals(2, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertTrue(result.contains(sampleStandup2), "contains second element")
            assertFalse(result.contains(sampleStandup3), "does not contain third element")
        }

        @Test
        @DisplayName("withUserIds")
        fun withUserIds() {
            val sampleId1 = UUID.randomUUID().toString()
            val sampleId2 = UUID.randomUUID().toString()
            val sampleId3 = UUID.randomUUID().toString()
            val sampleStandup1 = standupRepository.insert(Standup.example(userId = sampleId1))
            val sampleStandup2 = standupRepository.insert(Standup.example(userId = sampleId2))
            val sampleStandup3 = standupRepository.insert(Standup.example(userId = sampleId3))

            val result = standupRepository.find(withUserIds = setOf(sampleId1, sampleId2))

            assertEquals(2, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertTrue(result.contains(sampleStandup2), "contains second element")
            assertFalse(result.contains(sampleStandup3), "does not contain third element")
        }

        @Test
        @DisplayName("withBroadCastChannelId")
        fun withBroadCastChannelId() {

            val sampleId = UUID.randomUUID().toString()
            val sampleStandup1 = standupRepository.insert(Standup.example(broadcastChannelId = sampleId))
            val sampleStandup2 = standupRepository.insert(Standup.example())

            val result = standupRepository.find(withBroadcastChannelId = sampleId)

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("hasNumberOfAskedQuestions")
        fun hasNumberOfAskedQuestions() {
            val sampleStandup1 = standupRepository.insert(Standup.example(questionsAsked = 3))
            val sampleStandup2 = standupRepository.insert(Standup.example(questionsAsked = 0))

            val result = standupRepository.find(hasNumberOfAskedQuestions = 3)

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("timestampIsBefore")
        fun timestampIsBefore() {
            val sampleInstant = Instant.now()
            val sampleStandup1 = standupRepository.insert(Standup.example(timestamp = sampleInstant))
            val sampleStandup2 = standupRepository.insert(Standup.example(timestamp = sampleInstant.plusMillis(1)))

            val result = standupRepository.find(timestampIsBefore = sampleStandup2.timestamp)

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("timestampIsAfter")
        fun timestampIsAfter() {
            val sampleInstant = Instant.now()
            val sampleStandup1 = standupRepository.insert(Standup.example(timestamp = sampleInstant))
            val sampleStandup2 = standupRepository.insert(Standup.example(timestamp = sampleInstant.minusMillis(2)))

            val result = standupRepository.find(timestampIsAfter = sampleInstant.minusMillis(1))

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("hasAnswer")
        fun hasAnswer() {
            val sampleStandup1 = standupRepository.insert(Standup.example(answers = listOf(Standup.Answer("Some Answer", "1", 1),
                    Standup.Answer("Some Answer2", "2", 2))))
            val sampleStandup2 = standupRepository.insert(Standup.example(answers = listOf(Standup.Answer("Some Answer", "2", 1))))

            val result = standupRepository.find(hasAnswer = sampleStandup1.answers[1])

            assertEquals(1, result.size, "result size is correct")
            assertTrue(result.contains(sampleStandup1), "contains first element")
            assertFalse(result.contains(sampleStandup2), "does not contain second element")
        }

        @Test
        @DisplayName("offset")
        fun offset() {
            IntStream.rangeClosed(0, 9).forEach {
                standupRepository.insert(Standup.example())
            }

            val findAll = standupRepository.find(offset = 0)
            val findWithOffset = standupRepository.find(offset = 6)

            Assertions.assertEquals(10, findAll.size, "contains all")
            Assertions.assertEquals(4, findWithOffset.size, "contains remaining")
        }


        @Test
        @DisplayName("limit")
        fun limit() {
            IntStream.rangeClosed(0, 9).forEach {
                standupRepository.insert(Standup.example())
            }

            val findAll = standupRepository.find(limit = 10)
            val findWithOffset = standupRepository.find(limit = 1)

            Assertions.assertEquals(10, findAll.size, "contains all")
            Assertions.assertEquals(1, findWithOffset.size, "contains limited result")
        }

    }

}
