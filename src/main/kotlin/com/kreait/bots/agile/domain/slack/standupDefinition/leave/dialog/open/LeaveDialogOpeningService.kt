package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open

import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.Action
import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.Callback
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
class LeaveDialogOpeningService @Autowired constructor(private val slackClient: SlackClient,
                                                       private val message: MessageSourceAccessor) {

    companion object {
        private val LOG = LoggerFactory.getLogger(LeaveDialogOpeningService::class.java)

        private const val LEAVE_TITLE = "titleLeave"
        private const val LEAVE_LABEL = "labelLeave"
    }

    fun openStandupLeaveDialog(options: List<Options>, triggerId: String, userId: String, accessToken: String) {
        val request = SlackOpenDialogRequest(
                Dialog(
                        callback_id = Callback.LEAVE_DIALOG.id,
                        title = message.getMessage(LEAVE_TITLE, Locale.ENGLISH),
                        elements = listOf<Element>(
                                SelectElement(
                                        name = Action.SELECTED_STANDUP.id,
                                        label = message.getMessage(LEAVE_LABEL, Locale.ENGLISH),
                                        type = Type.SELECT,
                                        options = options)
                        )
                ), triggerId)

        this.slackClient.dialog()
                .open(accessToken)
                .with(request)
                .onFailure {
                    LOG.error("Failed to open leave dialog")
                }
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug("Successfully opened leave dialog")
                    }
                }
                .invoke()
    }
}
