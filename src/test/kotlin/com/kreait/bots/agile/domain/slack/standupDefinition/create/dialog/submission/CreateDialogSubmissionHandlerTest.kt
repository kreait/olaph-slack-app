package com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.submission

import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.ConversationService
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.Callback
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.User
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.common.messaging.Color
import com.kreait.slack.api.contract.jackson.group.users.ErrorInfoResponse
import com.kreait.slack.api.contract.jackson.group.users.InfoRequest
import com.kreait.slack.api.contract.jackson.group.users.SuccessfulInfoResponse
import com.kreait.slack.api.contract.jackson.group.users.sample
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CreateDialogSubmissionHandlerTest {

    @DisplayName("Test Successful Dialog submission Handler")
    @Test
    fun testHandler() {
        val slackClient = MockSlackClient()
        slackClient.users().info("").successResponse = SuccessfulInfoResponse.sample()
        slackClient.users().info("").failureResponse = ErrorInfoResponse.sample()
        val conversationService = mock<ConversationService>()

        val messageSender = mock<SlackMessageSender>()
        val standupDefinitionRepository = mock<StandupDefinitionRepository>() {
            on { insert(any<StandupDefinition>()) } doReturn StandupDefinition.sample()
        }
        val dialogSubmissionHandler = CreateDialogSubmissionHandler(slackClient, standupDefinitionRepository, conversationService, messageSender)
        val component = InteractiveMessage.sample().copy(
                submission = mapOf(
                        Pair(CreateDialogSubmission.NAME, "Workspace"),
                        Pair(CreateDialogSubmission.DAYS, "mon"),
                        Pair(CreateDialogSubmission.TIME, "13:30"),
                        Pair(CreateDialogSubmission.BROADCAST_CHANNEL_ID, "channel"),
                        Pair(CreateDialogSubmission.QUESTIONS, "what")
                ),
                responseUrl = "",
                team = InteractiveComponentResponse.Team("sampleTeam", "", "", ""),
                user = User("sampleUser", "", "", ""),
                callbackId = Callback.CREATION_DIALOG.id)

        dialogSubmissionHandler.handleCreateDialogSubmission(CreateDialogSubmission.of(component.submission!!), component,
                com.kreait.slack.broker.store.team.Team("", "",
                        com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                        com.kreait.slack.broker.store.team.Team.Bot("", "")))
        verify(messageSender, times(1)).sendEphemeralMessage(ResponseType.CREATION_SUCCESS,
                listOf(Attachment(title = "Workspace", fallback = "Standup defined :)", color = Color.GOOD)),
                MessageContext.of(component).copy(currentStandup = "Workspace"), "")
        val expectedParam = InfoRequest("sampleUser", true)
        Assertions.assertEquals(slackClient.users().info("").params(), expectedParam)
    }
}