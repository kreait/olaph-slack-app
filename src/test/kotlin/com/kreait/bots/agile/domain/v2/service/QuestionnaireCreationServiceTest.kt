package com.kreait.bots.agile.domain.v2.service

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.domain.v2.data.StandupSpec
import com.kreait.bots.agile.domain.v2.repository.QuestionnaireRepository
import com.kreait.bots.agile.domain.v2.repository.StandupSpecRepository
import com.kreait.bots.agile.domain.v2.sample
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestionnaireCreationServiceTest @Autowired constructor(private val standupSpecRepository: StandupSpecRepository,
                                                              private val questionnaireRepository: QuestionnaireRepository) {
    @BeforeEach
    fun setUp() {
        standupSpecRepository.save(StandupSpec.sample())
        questionnaireRepository.deleteAll()
    }

    @Test
    @DisplayName("Test Questionnaire Creation from StandupSpec")
    fun testCreation() {
        Assertions.assertEquals(0, questionnaireRepository.findAll().size)
        val questionnaireCreationService = QuestionnaireCreationService(standupSpecRepository, questionnaireRepository)
        questionnaireCreationService.createQuestionnaires()
        Assertions.assertEquals(1, questionnaireRepository.findAll().size)
    }

    @AfterEach
    fun tearDown() {
        standupSpecRepository.deleteAll()
        questionnaireRepository.deleteAll()
    }
}