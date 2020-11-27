package com.kreait.bots.agile.core.scheduler

import com.kreait.bots.agile.core.standup.close.StandupCancelService
import com.kreait.bots.agile.core.standup.create.StandupCreationService
import com.kreait.bots.agile.core.standup.open.StandupOpeningService
import com.kreait.bots.agile.core.standup.question.QuestionService
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SchedulerUnitTest {

    @DisplayName("test Opening Message")
    @Test
    fun testOpeningScheduler() {
        val openingService = mock<StandupOpeningService>()

        val scheduler = SQSListener(openingService, mock(), mock(), mock())
        val openingMessage = SQSListener.Type("Notification", SQSListener.Message.OPEN_STANDUPS)
        scheduler.onMessage(openingMessage)
        verify(openingService, times(1)).findAndOpenStandups()

    }

    @DisplayName("test Cancel Message")
    @Test
    fun testCancelScheduler() {
        val cancelService = mock<StandupCancelService>()

        val scheduler = SQSListener(mock(), mock(), mock(), cancelService)
        val cancelMessage = SQSListener.Type("Notification", SQSListener.Message.CANCEL_QUESTIONS)
        scheduler.onMessage(cancelMessage)
        verify(cancelService, times(1)).cancelOutdatedStandups()
    }

    @DisplayName("test Create Message")
    @Test
    fun testCreateScheduler() {
        val creationService = mock<StandupCreationService>()

        val scheduler = SQSListener(mock(), creationService, mock(), mock())
        val createMessage = SQSListener.Type("Notification", SQSListener.Message.CREATE_STANUPS)
        scheduler.onMessage(createMessage)
        verify(creationService, times(1)).createStandupsFromStandupDefinitions()

    }

    @DisplayName("test Questions Message")
    @Test
    fun testSendScheduler() {
        val questionService = mock<QuestionService>()
        val scheduler = SQSListener(mock(), mock(), questionService, mock())
        val sendMessage = SQSListener.Type("Notification", SQSListener.Message.SEND_QUESTIONS)
        scheduler.onMessage(sendMessage)
        verify(questionService, times(1)).sendQuestions()

    }
}