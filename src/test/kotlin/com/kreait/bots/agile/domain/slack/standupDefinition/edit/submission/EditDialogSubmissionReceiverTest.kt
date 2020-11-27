package com.kreait.bots.agile.domain.slack.standupDefinition.edit.submission

import com.kreait.bots.agile.domain.slack.InteractiveComponentReceiverTest
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class EditDialogSubmissionReceiverTest : InteractiveComponentReceiverTest {

    @DisplayName("Test Supports Component")
    @Test
    override fun supportsInteractiveMessage() {
        val editDialogSubmissionReceiver = EditDialogSubmissionReceiver(mock())
        val interactiveComponentResponse = InteractiveMessage.sample()
        editDialogSubmissionReceiver.supportsInteractiveMessage(interactiveComponentResponse)
    }

    @DisplayName("Test receiveInteractiveComponent")
    @Test
    override fun onReceiveInteractiveMessage() {
        val editDialogSubmissionHandler = mock<EditDialogSubmissionHandler>()
        val editDialogSubmissionReceiver = EditDialogSubmissionReceiver(editDialogSubmissionHandler)
        val interactiveComponentResponse = InteractiveMessage.sample().copy(submission = mapOf(Pair(CreateDialogSubmission.NAME, "Workspace"),
                Pair(CreateDialogSubmission.DAYS, "mon"),
                Pair(CreateDialogSubmission.TIME, "13:30"),
                Pair(CreateDialogSubmission.BROADCAST_CHANNEL_ID, "channel"),
                Pair(CreateDialogSubmission.QUESTIONS, "what")))
        editDialogSubmissionReceiver.onReceiveInteractiveMessage(interactiveComponentResponse, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        verify(editDialogSubmissionHandler, times(1)).handleEditDialogSubmission(
                CreateDialogSubmission.of(interactiveComponentResponse.submission!!), interactiveComponentResponse, "")
    }
}