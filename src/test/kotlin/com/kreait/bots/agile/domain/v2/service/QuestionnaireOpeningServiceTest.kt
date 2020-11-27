package com.kreait.bots.agile.domain.v2.service

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.domain.v2.data.Questionnaire
import com.kreait.bots.agile.domain.v2.repository.QuestionnaireRepository
import com.kreait.bots.agile.domain.v2.sample
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestionnaireOpeningServiceTest @Autowired constructor(private val questionnaireRepository: QuestionnaireRepository) {
    private var savedQuestionnaire: Questionnaire? = null

    @BeforeEach
    fun setup() {
        savedQuestionnaire = questionnaireRepository.save(Questionnaire.sample().copy(time = LocalTime.of(1, 0)))
    }

    @Test
    @DisplayName("Test Questionnaire Opening from StandupSpec")
    fun openQuestionnaires() {
        val questionnaireOpeningService = QuestionnaireOpeningService(questionnaireRepository, mock())
        questionnaireOpeningService.openQuestionnaires()
        Assertions.assertEquals(
                savedQuestionnaire?.copy(lifecycleStatus = Questionnaire.LifecycleStatus.OPEN),
                questionnaireRepository.findById(savedQuestionnaire?.id!!).get()
        )
    }

    @AfterEach
    fun tearDown() {
        questionnaireRepository.deleteAll()
    }
}