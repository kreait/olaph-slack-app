package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open

import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class LeaveCommandSubmissionHandlerTest {

    @DisplayName("Test Leave command submission handler")
    @Test
    fun testLeaveCommandHandler() {
        val messageSender = mock<SlackMessageSender>()
        val standupRepository = mock<StandupRepository>()
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } doReturn listOf(StandupDefinition.sample().copy(id = "sampleStandup"))
        }
        val leaveCommandSubmissionHandler = LeaveCommandSubmissionHandler(standupDefinitionRepository, standupRepository, messageSender)
        val component = InteractiveMessage.sample()

        leaveCommandSubmissionHandler.handleSubmission(component, "")
    }
}