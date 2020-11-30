package com.kreait.bots.agile.core.standup.answer

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.common.example
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@IntegrationTest
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
internal class PredicatesTest @Autowired constructor(private val standupRepository: StandupRepository) {

    private val predicates = Standup.Predicates(this.standupRepository)
    private val answer1 = Standup.Answer.example(eventId = "1", eventTime = 1)
    private val answer2 = Standup.Answer.example(eventId = "2", eventTime = 2)
    private val userId1 = "userId1"
    private val userId2 = "userId2"

    @BeforeEach
    fun setUp() {
        this.standupRepository.deleteAll()
    }

    @Nested
    inner class UserTests {

        @DisplayName("Save new answer")
        @Test
        fun testCorrectUnsavedAnswer() {
            standupRepository.insert(Standup.example(userId = userId1, answers = listOf(answer1)))
            standupRepository.insert(Standup.example(userId = userId2))

            Assertions.assertTrue(predicates.canSaveAnswer(userId1, answer2))
            Assertions.assertTrue(predicates.canSaveAnswer(userId2, answer1))
        }

        @DisplayName("Save already existing answer")
        @Test
        fun testWrongAnswerExistsForUser() {
            standupRepository.insert(Standup.example(userId = userId1, answers = listOf(answer1, answer2)))

            Assertions.assertFalse(predicates.canSaveAnswer(userId1, answer1))
        }
    }
}
