package com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.submission

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.Action
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.Callback
import com.kreait.slack.api.contract.jackson.Channel
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.User
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class DeleteDialogSubmissionHandlerTest {

    @DisplayName("test Delete Dialog Submission Handler")
    @Test
    fun testHandler() {
        val slackClient = MockSlackClient()
        val standup = StandupDefinition.sample().copy(id = "sampleId")
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                findById(any(), any(), any())
            } doReturn standup
        }
        val standupRepository = mock<StandupRepository>()
        val messageSender = mock<SlackMessageSender> {}
        val deleteDialogSubmissionHandler = DeleteDialogSubmissionHandler(slackClient, standupDefinitionRepository, standupRepository, messageSender)
        val component = InteractiveMessage.sample().copy(callbackId = Callback.DELETION_DIALOG.id,
                channel = Channel.sample().copy("channelId"),
                responseUrl = "",
                submission = mapOf(Pair(Action.SELECTED_STANDUP.id, "sampleId")),
                team = InteractiveComponentResponse.Team.sample().copy("teamId"),
                user = User.sample().copy("userId"))

        deleteDialogSubmissionHandler.handleDeleteDialogSubmission(component, "")
        verify(messageSender, times(1)).sendEphemeralMessage(ResponseType.DELETE_SUCCESS,
                messageContext = MessageContext.of(component).copy(currentStandup = standup.name), token = "")
    }
}