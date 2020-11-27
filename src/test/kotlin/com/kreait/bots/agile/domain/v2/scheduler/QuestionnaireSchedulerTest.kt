package com.kreait.bots.agile.domain.v2.scheduler

import com.kreait.bots.agile.domain.v2.service.QuestionnaireCreationService
import com.kreait.bots.agile.domain.v2.service.QuestionnaireOpeningService
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import java.lang.RuntimeException

class QuestionnaireSchedulerTest {

    @Test
    fun testCreateMessage() {
        val questionnaireCreationService = mock<QuestionnaireCreationService>()
        val questionnaireOpeningService = mock<QuestionnaireOpeningService>()
        val questionnaireScheduler = QuestionnaireScheduler(questionnaireCreationService, questionnaireOpeningService)

        questionnaireScheduler.onMessage(
                QuestionnaireScheduler.SqsMessage("", QuestionnaireScheduler.Message.CREATE_QUESTIONNAIRES)
        )
        verify(questionnaireCreationService, times(1)).createQuestionnaires()
    }

    @Test
    fun testOpenMessage() {
        val questionnaireCreationService = mock<QuestionnaireCreationService>()
        val questionnaireOpeningService = mock<QuestionnaireOpeningService>()
        val questionnaireScheduler = QuestionnaireScheduler(questionnaireCreationService, questionnaireOpeningService)

        questionnaireScheduler.onMessage(
                QuestionnaireScheduler.SqsMessage("", QuestionnaireScheduler.Message.OPEN_QUESTIONNAIRES)
        )
        verify(questionnaireOpeningService, times(1)).openQuestionnaires()
    }

    @Test
    fun testExceptionCaching() {
        val questionnaireCreationService = mock<QuestionnaireCreationService>()
        val questionnaireOpeningService = mock<QuestionnaireOpeningService> {
            on { openQuestionnaires() } doThrow RuntimeException()
        }
        val questionnaireScheduler = QuestionnaireScheduler(questionnaireCreationService, questionnaireOpeningService)

        questionnaireScheduler.onMessage(
                QuestionnaireScheduler.SqsMessage("", QuestionnaireScheduler.Message.OPEN_QUESTIONNAIRES)
        )
    }
}