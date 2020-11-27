package com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.open

import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.Action
import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.Callback
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.group.dialog.Dialog
import com.kreait.slack.api.contract.jackson.group.dialog.Element
import com.kreait.slack.api.contract.jackson.group.dialog.Options
import com.kreait.slack.api.contract.jackson.group.dialog.SelectElement
import com.kreait.slack.api.contract.jackson.group.dialog.SlackOpenDialogRequest
import com.kreait.slack.api.contract.jackson.group.dialog.Type
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class JoinDialogOpeningService @Autowired constructor(private val slackClient: SlackClient,
                                                      private val message: MessageSourceAccessor) {

    companion object {
        private val LOG = LoggerFactory.getLogger(JoinDialogOpeningService::class.java)

        private const val JOIN_TITLE = "titleJoin"
        private const val JOIN_LABEL = "labelJoin"
    }

    /**
     * triggers the Join-Dialog
     * @param options list of options of standupdefinitions that can be selected
     * @param triggerId the trigger to open the dialog
     */

    fun openStandupJoinDialog(options: List<Options>, triggerId: String, userId: String, accessToken: String) {
        val request = SlackOpenDialogRequest(Dialog(
                callback_id = Callback.JOIN_DIALOG.id,
                title = message.getMessage(JOIN_TITLE, Locale.ENGLISH),
                elements = listOf<Element>(SelectElement(
                        name = Action.SELECTED_STANDUP.id,
                        label = message.getMessage(JOIN_LABEL, Locale.ENGLISH),
                        type = Type.SELECT,
                        options = options))
        ), triggerId)

        this.slackClient.dialog()
                .open(accessToken)
                .with(request)
                .onFailure {
                    LOG.error("Failed to open standup join-dialog")
                }
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug("Successfully opened standup join-dialog")
                    }
                }
                .invoke()
    }

}
