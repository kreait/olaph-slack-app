package com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.submission

import com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.Callback
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
open class DeleteDialogSubmissionReceiver(private val deleteDialogSubmissionHandler: DeleteDialogSubmissionHandler) : InteractiveComponentReceiver<InteractiveMessage> {

    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        this.deleteDialogSubmissionHandler.handleDeleteDialogSubmission(interactiveComponentResponse, team.bot.accessToken)
    }

    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean {
        return interactiveComponentResponse.callbackId == Callback.DELETION_DIALOG.id
    }
}
