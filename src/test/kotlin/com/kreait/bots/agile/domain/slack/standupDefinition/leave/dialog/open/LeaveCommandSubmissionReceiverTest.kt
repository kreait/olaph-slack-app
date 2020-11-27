package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open

import com.kreait.bots.agile.domain.slack.InteractiveComponentReceiverTest
import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.Callback
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.User
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class LeaveCommandSubmissionReceiverTest : InteractiveComponentReceiverTest {

    @DisplayName("test supports Leave submission")
    @Test
    override fun supportsInteractiveMessage() {
        val leaveCommandSubmissionReceiver = LeaveCommandSubmissionReceiver(mock())
        val component = InteractiveMessage.sample().copy(
                team = InteractiveComponentResponse.Team("sampleTeam", "", "", ""),
                user = User("sampleUser", "", "", ""),
                callbackId = Callback.LEAVE_DIALOG.id)
        leaveCommandSubmissionReceiver.supportsInteractiveMessage(component)
    }

    @DisplayName("test receive Leave submission")
    @Test
    override fun onReceiveInteractiveMessage() {
        val leaveCommandSubmissionHandler = mock<LeaveCommandSubmissionHandler>()
        val leaveCommandSubmissionReceiver = LeaveCommandSubmissionReceiver(leaveCommandSubmissionHandler)
        val component = InteractiveMessage.sample()
        leaveCommandSubmissionReceiver.onReceiveInteractiveMessage(component, HttpHeaders.EMPTY, com.kreait.slack.broker.store.team.Team("", "",
                com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                com.kreait.slack.broker.store.team.Team.Bot("", "")))

        verify(leaveCommandSubmissionHandler, times(1)).handleSubmission(component, "")
    }
}