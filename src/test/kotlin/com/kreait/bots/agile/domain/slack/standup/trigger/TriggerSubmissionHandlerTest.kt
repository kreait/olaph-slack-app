package com.kreait.bots.agile.domain.slack.standup.trigger

import com.kreait.bots.agile.core.standup.create.StandupCreationService
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.core.standup.question.QuestionService
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.standup.SlackOpeningMessageSender
import com.kreait.slack.api.contract.jackson.Channel
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.User
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TriggerSubmissionHandlerTest {

    @DisplayName("Test Trigger Submission Handler")
    @Test
    fun testHandler() {
        val standup = Standup.sample()
        val standupDefinition = StandupDefinition.sample()

        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                findById(any(), any(), any())
            } doReturn standupDefinition
        }
        val standupRepository = mock<StandupRepository> {
            on { exists(any(), any(), any(), any(), any()) } doReturn false
            on {
                find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } doReturn listOf(standup)
        }
        val standupCreationService = mock<StandupCreationService>()
        val slackOpeningMessageSender = mock<SlackOpeningMessageSender>()
        val questionService = mock<QuestionService>()
        val component = InteractiveMessage.sample().copy(channel = Channel("channelId", ""),
                submission = mapOf(Pair(TriggerStandupDialog.STANDUP_SELECTION, "selected")),
                team = InteractiveComponentResponse.Team.sample().copy(id = "teamId"),
                user = User.sample().copy(id = "userId"))

        val triggerSubmissionHandler = TriggerSubmissionHandler(standupDefinitionRepository, standupRepository, standupCreationService,
                slackOpeningMessageSender, questionService)
        triggerSubmissionHandler.handleSubmission(component)
        verify(standupCreationService, times(1)).createStandupsForStandupDefinition(standupDefinition)
        verify(slackOpeningMessageSender, times(1)).sendOpeningMessage(standup)
        verify(questionService, times(1)).sendQuestions("userId")
    }
}