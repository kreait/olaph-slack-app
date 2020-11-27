package com.kreait.bots.agile.domain.slack.standupDefinition.edit.submission

import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.Callback
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class EditDialogSubmissionReceiver @Autowired constructor(private val editDialogSubmissionHandler: EditDialogSubmissionHandler) : InteractiveComponentReceiver<InteractiveMessage> {

    /**
     * Supports Dialogs with with EDIT_DIALOG Callback id
     */
    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean {
        return interactiveComponentResponse.callbackId.equals(Callback.EDIT_DIALOG.id)
    }

    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        this.editDialogSubmissionHandler.handleEditDialogSubmission(CreateDialogSubmission.of(
                interactiveComponentResponse.submission!!),
                interactiveComponentResponse, team.bot.accessToken)
    }
}