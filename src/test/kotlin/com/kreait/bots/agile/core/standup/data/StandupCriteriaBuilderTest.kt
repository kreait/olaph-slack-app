package com.kreait.bots.agile.core.standup.data

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.common.example
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupCriteria
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.query.Query
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
internal class StandupStandupCriteriaBuilderTest(@Autowired val standupRepository: StandupRepository) {

    @BeforeEach
    fun setUp() {
        this.standupRepository.deleteAll()
    }

    @Nested
    inner class TestStringProperties {

        @Test
        @DisplayName("test string criteria")
        fun testUserId() {
            val standup = standupRepository.insert(Standup.example(userId = "user1"))
            standupRepository.insert(Standup.example(userId = "user2"))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(userId = standup.userId)))

            Assertions.assertEquals(1, standups.size)
            Assertions.assertEquals(standup.userId, standups.first().userId)
        }

    }

    @Nested
    inner class TestStatus {

        @Test
        @DisplayName("test status criteria")
        fun testStatus() {
            val standup = standupRepository.insert(Standup.example(status = Standup.Status.OPEN))
            standupRepository.insert(Standup.example(status = Standup.Status.CANCELLED))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(status = standup.status)))

            Assertions.assertEquals(1, standups.size)
            Assertions.assertEquals(standup.status, standups.first().status)

        }

        @Test
        @DisplayName("test closed status")
        fun testClosedStatus() {
            standupRepository.insert(Standup.example(status = Standup.Status.OPEN))
            standupRepository.insert(Standup.example(status = Standup.Status.CANCELLED))

            val standupsNotClosed = standupRepository.findStandups(
                    Query.query(StandupCriteria.of(notStatus = setOf(Standup.Status.CLOSED))))
            val standupsNotOpen = standupRepository.findStandups(
                    Query.query(StandupCriteria.of(notStatus = setOf(Standup.Status.OPEN))))
            val standupsNeitherOpenNorCancelled = standupRepository.findStandups(
                    Query.query(StandupCriteria.of(notStatus = (setOf(Standup.Status.OPEN, Standup.Status.CANCELLED)))))

            Assertions.assertEquals(2, standupsNotClosed.size)
            Assertions.assertEquals(1, standupsNotOpen.size)
            Assertions.assertEquals(0, standupsNeitherOpenNorCancelled.size)
        }
    }

    @Nested
    inner class TestDate {

        @Test
        @DisplayName("test date criteria")
        fun testDate() {
            val standup = standupRepository.insert(Standup.example(date = LocalDate.now()))
            standupRepository.insert(Standup.example(date = LocalDate.now().minusDays(1)))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(date = LocalDate.now())))

            Assertions.assertEquals(1, standups.size)
            Assertions.assertEquals(standup.id, standups.first().id)

        }

        @Test
        @DisplayName("test past date criteria")
        fun testDateLt() {
            standupRepository.insert(Standup.example(date = LocalDate.now()))
            val standup = standupRepository.insert(Standup.example(date = LocalDate.now().minusDays(1)))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(dateOlderThan = LocalDate.now())))

            Assertions.assertEquals(1, standups.size)
            Assertions.assertEquals(standup.id, standups.first().id)

        }

    }

    @Nested
    inner class TestTime {

        @Test
        @DisplayName("test time criteria")
        fun testTime() {
            val standup = standupRepository.insert(Standup.example(time = LocalTime.now()))
            standupRepository.insert(Standup.example(time = LocalTime.now().plusHours(2)))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(time = LocalTime.now())))

            Assertions.assertEquals(1, standups.size)
            Assertions.assertEquals(standup.id, standups.first().id)

        }

        @Test
        @DisplayName("test past time criteria")
        fun testTimeLte() {
            standupRepository.insert(Standup.example(time = LocalTime.of(16, 15)))
            val standup1 = standupRepository.insert(Standup.example(time = LocalTime.of(12, 30)))
            val standup2 = standupRepository.insert(Standup.example(time = LocalTime.of(6, 0)))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(timeOlderEquals = LocalTime.of(12, 30))))

            Assertions.assertEquals(2, standups.size)
            Assertions.assertEquals(standup1.id, standups[0].id)
            Assertions.assertEquals(standup2.id, standups[1].id)

        }


    }

    @Nested
    inner class TestStringListAll {

        private val question1: String = "question1"
        private val question2: String = "question2"
        private val question3: String = "question3"
        private val question4: String = "question4"
        private val question5: String = "question5"

        @Test
        fun testQuestion() {
            val standup1 = standupRepository.insert(Standup.example(questions = listOf(question1, question2)))
            standupRepository.insert(Standup.example(questions = listOf(question3, question4)))

            val standups1 = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(questions = listOf(question2)))
            )

            Assertions.assertEquals(1, standups1.size)
            Assertions.assertEquals(standup1.questions[0], standups1.first().questions[0])
        }

        @Test
        fun testQuestionsWrongQuery() {
            standupRepository.insert(Standup.example(questions = listOf(question1, question2)))
            standupRepository.insert(Standup.example(questions = listOf(question3, question4)))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(questions = listOf(question1, question3)))
            )

            Assertions.assertEquals(0, standups.size)
        }

        @Test
        fun testQuestionsCorrectQuery() {
            val standup1 = standupRepository.insert(Standup.example(questions = listOf(question1, question2)))
            val standup2 = standupRepository.insert(Standup.example(questions = listOf(question1, question5)))
            standupRepository.insert(Standup.example(questions = listOf(question3, question4)))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(questions = listOf(question1)))
            )

            Assertions.assertEquals(2, standups.size)
            Assertions.assertEquals(standup1.questions[0], standups.first().questions[0])
            Assertions.assertEquals(standup2.questions[1], standups[1].questions[1])
        }

        @Test
        fun testOrder() {
            val standup1 = standupRepository.insert(Standup.example(questions = listOf(question1, question2)))

            val standups1 = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(questions = listOf(question2, question1)))
            )

            Assertions.assertEquals(1, standups1.size)
            Assertions.assertEquals(standup1.questions[0], standups1.first().questions[0])
        }
    }

    @Nested
    inner class TestInt {

        @Test
        @DisplayName("test int criteria")
        fun testUserId() {
            val standup = standupRepository.insert(Standup.example(questionsAsked = 2))
            standupRepository.insert(Standup.example(questionsAsked = 1))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(questionsAsked = standup.questionsAsked)))

            Assertions.assertEquals(1, standups.size)
            Assertions.assertEquals(standup.questionsAsked, standups.first().questionsAsked)
        }
    }

    @Nested
    inner class TestAnswers {

        private val answerText1: String = "answer1"
        private val answerText2: String = "answer2"
        private val answerText3: String = "answer3"

        private val answerEventId1: String = "eventId1"
        private val answerEventId2: String = "eventId2"

        private val answerEventTime1: Int = 10
        private val answerEventTime2: Int = 11

        @Test
        @DisplayName("test nested array criteria")
        fun testAnswerText() {
            val standup = standupRepository.insert(Standup.example(answers = listOf(Standup.Answer.example(text = answerText1))))
            standupRepository.insert(Standup.example(answers = listOf(Standup.Answer.example(answerText2))))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(answers = listOf(answerText1, answerText3))))

            Assertions.assertEquals(1, standups.size)
            Assertions.assertEquals(standup.answers[0].text, standups.first().answers[0].text)
        }

        @Test
        @DisplayName("test Answer criteria")
        fun testAnswer() {
            val answer1 = Standup.Answer.example(text = answerText1, eventId = answerEventId1, eventTime = answerEventTime1)
            val answer2 = Standup.Answer.example(text = answerText2, eventId = answerEventId2, eventTime = answerEventTime2)

            val standup = standupRepository.insert(Standup.example(answers = listOf(answer1)))
            standupRepository.insert(Standup.example(answers = listOf(answer2)))

            val standups = standupRepository.findStandups(
                    Query.query(StandupCriteria.example(answer = answer1)))

            Assertions.assertEquals(1, standups.size)
            Assertions.assertEquals(standup.answers[0].text, standups.first().answers[0].text)
        }


    }

}
