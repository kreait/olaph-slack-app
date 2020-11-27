package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open

import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.Callback
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class LeaveCommandSubmissionReceiver @Autowired constructor(private val leaveCommandSubmissionHandler: LeaveCommandSubmissionHandler) : InteractiveComponentReceiver<InteractiveMessage> {

    /**
     * checks if the interactive message can be handled
     * [returns] true when the received submission is for leaving the dialog
     */
    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean {
        return interactiveComponentResponse.callbackId == Callback.LEAVE_DIALOG.id
    }

    /**
     * retreives the interactive component and handles it
     */
    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        leaveCommandSubmissionHandler.handleSubmission(interactiveComponentResponse, team.bot.accessToken)
    }
}