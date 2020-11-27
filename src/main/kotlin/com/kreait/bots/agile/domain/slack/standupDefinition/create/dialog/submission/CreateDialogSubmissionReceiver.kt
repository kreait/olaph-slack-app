package com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.submission

import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.Callback
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class CreateDialogSubmissionReceiver @Autowired constructor(private val createDialogSubmissionHandler: CreateDialogSubmissionHandler) : InteractiveComponentReceiver<InteractiveMessage> {

    /**
     * Supports Dialogs with [OpenDialogService.DIALOG_CALLBACK_ID]
     */
    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean {
        return interactiveComponentResponse.callbackId == Callback.CREATION_DIALOG.id
    }

    /**
     * Receives [InteractiveComponentResponse]s from open dialog requests
     */
    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        this.createDialogSubmissionHandler.handleCreateDialogSubmission(CreateDialogSubmission.of(interactiveComponentResponse.submission!!),
                interactiveComponentResponse, team)
    }

    override fun shouldThrowException(exception: Exception): Boolean {
        return true
    }
}
