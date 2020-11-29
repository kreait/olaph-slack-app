package com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.submission

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.Action
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class JoinDialogSubmissionHandlerTest {

    @DisplayName("test handleJoinSubmission")
    @Test
    fun testHandleJoinSubmission() {
        val slackClient = MockSlackClient()
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                findById(any(), any(), any())
            } doReturn StandupDefinition.sample().copy(id = "sampleStandup")
        }
        val messageSender = mock<SlackMessageSender>()
        val handleJoinSubmission = JoinDialogSubmissionHandler(slackClient, standupDefinitionRepository, messageSender)
        val component = InteractiveMessage.sample().copy(responseUrl = "",
                submission = mapOf(Pair(Action.SELECTED_STANDUP.id, "sampleStandup")),
                team = InteractiveComponentResponse.Team.sample().copy("sampleTeam"))


        handleJoinSubmission.handleJoinSubmission(component, "")
        verify(messageSender, times(1)).sendEphemeralMessage(responseType = eq(ResponseType.SUCCESS_JOIN),
                attachments = any(),
                messageContext = eq(MessageContext.of(component).copy(currentStandup = "Workspace")),
                token = eq(""))
    }
}