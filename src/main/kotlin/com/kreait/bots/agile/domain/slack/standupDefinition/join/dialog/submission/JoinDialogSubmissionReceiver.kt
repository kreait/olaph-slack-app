package com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.submission

import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.Callback
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class JoinDialogSubmissionReceiver(private val joinDialogSubmissionHandler: JoinDialogSubmissionHandler) : InteractiveComponentReceiver<InteractiveMessage> {

    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean {
        return interactiveComponentResponse.callbackId == Callback.JOIN_DIALOG.id
    }

    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        this.joinDialogSubmissionHandler.handleJoinSubmission(interactiveComponentResponse, team.bot.accessToken)
    }
}