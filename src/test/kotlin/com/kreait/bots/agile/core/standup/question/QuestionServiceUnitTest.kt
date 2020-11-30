package com.kreait.bots.agile.core.standup.question

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.standup.SlackQuestionSender
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

@IntegrationTest
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
class QuestionServiceUnitTest @Autowired constructor(private val standupRepository: StandupRepository) {

    @BeforeEach
    fun setup() {
        standupRepository.deleteAll()
    }


    @DisplayName("Test question service")
    @Test
    fun testQuestionService() {
        standupRepository.insert(Standup.sample().copy(id = "sampleStandup",
                userId = "sampleUser",
                status = Standup.Status.OPEN,
                questions = listOf("Question1", "Question2"),
                answers = listOf(Standup.Answer("answer1", "event", 0)),
                questionsAsked = 1))
        val questionSender = mock<SlackQuestionSender> { on { sendQuestion(any(), any(), any()) } doReturn true }
        val questionService = QuestionService(standupRepository, questionSender)
        questionService.sendQuestions("sampleUser")
    }
}