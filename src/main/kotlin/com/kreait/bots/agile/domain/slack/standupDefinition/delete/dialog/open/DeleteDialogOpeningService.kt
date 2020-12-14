package com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.open

import com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.Action
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.Callback
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.group.dialog.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class DeleteDialogOpeningService @Autowired constructor(private val slackClient: SlackClient,
                                                        private val message: MessageSourceAccessor) {
    companion object {
        private val LOG = LoggerFactory.getLogger(DeleteDialogOpeningService::class.java)

        private const val DELETE_TITLE = "titleDelete"
        private const val DELETE_LABEL = "labelDelete"
    }

    /**
     *  triggers the Deletion Dialog
     * @param options list of options of standupdefinitions that can be deleted
     * @param triggerId the triggerid to open the dialog
     */

    fun openStandupDeletionDialog(options: List<Options>, triggerId: String, userId: String, accessToken: String) {

        val userLocale = Locale.ENGLISH

        val request = OpenDialogRequest(dialog = Dialog(Callback.DELETION_DIALOG.id, message.getMessage(DELETE_TITLE, userLocale),
                elements = listOf<Element>(
                        SelectElement(
                                name = Action.SELECTED_STANDUP.id,
                                label = message.getMessage(DELETE_LABEL, userLocale),
                                type = Type.SELECT,
                                options = options
                        ))),
                trigger_id = triggerId)

        this.slackClient.dialog()
                .open(accessToken)
                .with(request)
                .onFailure {
                    LOG.error("Failed to open standup-deletion dialog")
                }
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug("Successfully opened standup-deletion dialog")
                    }
                }
                .invoke()
    }

}
