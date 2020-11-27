package com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.submission

import com.kreait.bots.agile.domain.slack.InteractiveComponentReceiverTest
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class DeleteDialogSubmissionReceiverTest : InteractiveComponentReceiverTest {

    private lateinit var component: InteractiveMessage

    @BeforeEach
    fun setup() {
        component = InteractiveMessage.sample()
    }

    @DisplayName("Test Supports Method")
    @Test
    override fun supportsInteractiveMessage() {
        val deleteDialogSubmissionReceiver = DeleteDialogSubmissionReceiver(mock())
        deleteDialogSubmissionReceiver.supportsInteractiveMessage(component)
    }

    @DisplayName("Test receive Method")
    @Test
    override fun onReceiveInteractiveMessage() {
        val handler = mock<DeleteDialogSubmissionHandler>()
        val deleteDialogSubmissionReceiver = DeleteDialogSubmissionReceiver(handler)
        deleteDialogSubmissionReceiver.onReceiveInteractiveMessage(component, HttpHeaders.EMPTY, com.kreait.slack.broker.store.team.Team("", "",
                com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                com.kreait.slack.broker.store.team.Team.Bot("", "")))
        verify(handler, times(1)).handleDeleteDialogSubmission(component, "")
    }

}