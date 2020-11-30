package com.kreait.bots.agile.core.standup.answer

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.domain.slack.standup.BroadcastingService
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.core.standup.question.QuestionService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension


@IntegrationTest
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
class StandupAnswerReceiverUnitTest @Autowired constructor(private val standupRepository: StandupRepository) {

    @BeforeEach
    fun setup() {
        standupRepository.deleteAll()
    }


    @DisplayName("Test Answer Receiver")
    @Test
    fun testAnswerReceiver() {
        standupRepository.insert(Standup.sample().copy(status = Standup.Status.OPEN,
                userId = "sampleUser", teamId = "sampleTeam"))
        val broadcastingString = mock<BroadcastingService>()
        val questionService = mock<QuestionService>()
        val answerReceiver = StandupAnswerReceiver(standupRepository, broadcastingString, questionService, mock())
        answerReceiver.handleAnswer("sampleUser", "sampleAnswer", "sampleEventId",
                0, "sampleTeam")

    }

    @DisplayName("Test Wrong Answer Receiver")
    @Test
    fun testWrongAnswerReceiver() {
        standupRepository.insert(Standup.sample().copy(status = Standup.Status.CLOSED,
                userId = "sampleUser", teamId = "sampleTeam"))
        val broadcastingString = mock<BroadcastingService>()
        val questionService = mock<QuestionService>()
        val answerReceiver = StandupAnswerReceiver(standupRepository, broadcastingString, questionService, mock())
        answerReceiver.handleAnswer("sampleUser", "sampleAnswer", "sampleEventId",
                0, "sampleTeam")

    }

    @DisplayName("Test MultipleOpenStandups Exception")
    @Test
    fun testMultipleOpenException() {
        standupRepository.insert(Standup.sample().copy(status = Standup.Status.OPEN,
                userId = "sampleUser", teamId = "sampleTeam"))
        standupRepository.insert(Standup.sample().copy(status = Standup.Status.OPEN,
                userId = "sampleUser", teamId = "sampleTeam"))
        val broadcastingString = mock<BroadcastingService>()
        val questionService = mock<QuestionService>()
        val answerReceiver = StandupAnswerReceiver(standupRepository, broadcastingString, questionService, mock())
        Assertions.assertThrows(MultipleOpenStandupsException::class.java) {
            answerReceiver.handleAnswer("sampleUser", "sampleAnswer", "sampleEventId",
                    0, "sampleTeam")
        }
    }
}