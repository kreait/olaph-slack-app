package com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.submission

import com.kreait.bots.agile.domain.slack.InteractiveComponentReceiverTest
import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.Action
import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.Callback
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class JoinDialogSubmissionReceiverTest : InteractiveComponentReceiverTest {

    @DisplayName("")
    @Test
    override fun supportsInteractiveMessage() {
        val component = InteractiveMessage.sample().copy(submission = mapOf(Pair(Action.SELECTED_STANDUP.id, "sampleStandup")),
                team = InteractiveComponentResponse.Team.sample().copy(id = "sampleTeam"),
                callbackId = Callback.JOIN_DIALOG.id)

        val joinDialogSubmissionReceiver = JoinDialogSubmissionReceiver(mock())
        joinDialogSubmissionReceiver.supportsInteractiveMessage(component)
    }

    override fun onReceiveInteractiveMessage() {
        val component = InteractiveMessage.sample().copy(
                submission = mapOf(Pair(Action.SELECTED_STANDUP.id, "sampleStandup")),
                team = InteractiveComponentResponse.Team.sample().copy("sampleTeam"),
                callbackId = Callback.JOIN_DIALOG.id)

        val joinDialogSubmissionHandler = mock<JoinDialogSubmissionHandler>()
        val joinDialogSubmissionReceiver = JoinDialogSubmissionReceiver(joinDialogSubmissionHandler)
        joinDialogSubmissionReceiver.onReceiveInteractiveMessage(component, HttpHeaders.EMPTY, mock())
        verify(joinDialogSubmissionHandler, times(1)).handleJoinSubmission(component, "")
    }
}
