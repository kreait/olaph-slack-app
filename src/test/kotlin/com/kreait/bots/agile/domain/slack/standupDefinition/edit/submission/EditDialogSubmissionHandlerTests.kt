package com.kreait.bots.agile.domain.slack.standupDefinition.edit.submission

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.ConversationService
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.Callback
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.User
import com.kreait.slack.api.contract.jackson.common.types.Member
import com.kreait.slack.api.contract.jackson.common.types.sample
import com.kreait.slack.api.contract.jackson.group.users.InfoRequest
import com.kreait.slack.api.contract.jackson.group.users.SuccessfulInfoResponse
import com.kreait.slack.api.contract.jackson.group.users.sample
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class EditDialogSubmissionHandlerTests {

    @DisplayName("Test Edit Dialog Submission   ")
    @Test
    fun testEditDialogSubmission() {
        val slackClient = MockSlackClient()
        slackClient.users().info("").successResponse = SuccessfulInfoResponse(true, Member.sample())
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on { findById(any(), any(), any()) } doReturn StandupDefinition.sample()
            on { save(any<StandupDefinition>()) } doReturn StandupDefinition.sample().copy(id = "persisted")
        }
        val conversationService = mock<ConversationService>()
        val editDialogSubmissionHandler = EditDialogSubmissionHandler(
            slackClient, standupDefinitionRepository,
            conversationService, mock(), mock(), mock()
        )

        val component = InteractiveMessage.sample().copy(
            submission = mapOf(
                Pair(CreateDialogSubmission.NAME, "Workspace"),
                Pair(CreateDialogSubmission.DAYS, "mon"),
                Pair(CreateDialogSubmission.TIME, "13:30"),
                Pair(CreateDialogSubmission.BROADCAST_CHANNEL_ID, "channel"),
                Pair(CreateDialogSubmission.QUESTIONS, "what")
            ),
            team = InteractiveComponentResponse.Team.sample().copy("sampleTeam"),
            user = User.sample().copy("sampleUserId"),
            callbackId = Callback.EDIT_DIALOG.id,
            responseUrl = ""
        )

        editDialogSubmissionHandler.handleEditDialogSubmission(CreateDialogSubmission.of(component.submission!!), component, "")
        val expectedParam = InfoRequest.sample().copy("sampleUserId", true)
        Assertions.assertEquals(slackClient.users().info("").params(), expectedParam)
    }
}