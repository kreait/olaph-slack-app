package com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.submission

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.slack.InteractiveComponentReceiverTest
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.Callback
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

@UnitTest
class CreateDialogSubmissionReceiverTest : InteractiveComponentReceiverTest {

    @DisplayName("Test supportsInteractiveMessage")
    @Test
    override fun supportsInteractiveMessage() {
        val createDialogSubmissionReceiver = CreateDialogSubmissionReceiver(mock())
        //TODO Replace with extension method
        createDialogSubmissionReceiver.supportsInteractiveMessage(InteractiveMessage.sample())

    }

    @DisplayName("Test onReceiveInteractiveMessage")
    @Test
    override fun onReceiveInteractiveMessage() {
        val createDialogSubmissionHandler = mock<CreateDialogSubmissionHandler>()
        val createDialogSubmissionReceiver = CreateDialogSubmissionReceiver(createDialogSubmissionHandler)

        val team = com.kreait.slack.broker.store.team.Team("", "",
                com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                com.kreait.slack.broker.store.team.Team.Bot("", ""))
        val component = InteractiveMessage.sample().copy(
                submission = mapOf(
                        Pair(CreateDialogSubmission.NAME, "test"),
                        Pair(CreateDialogSubmission.DAYS, "mon"),
                        Pair(CreateDialogSubmission.TIME, "13:30"),
                        Pair(CreateDialogSubmission.BROADCAST_CHANNEL_ID, "test"),
                        Pair(CreateDialogSubmission.QUESTIONS, "test")),
                callbackId = Callback.CREATION_DIALOG.id)
        createDialogSubmissionReceiver.onReceiveInteractiveMessage(component, HttpHeaders.EMPTY, com.kreait.slack.broker.store.team.Team("", "",
                com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                com.kreait.slack.broker.store.team.Team.Bot("", "")))
        verify(createDialogSubmissionHandler, times(1)).handleCreateDialogSubmission(
                CreateDialogSubmission.of(component.submission!!), component, team)
    }
}