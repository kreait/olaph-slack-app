package com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.open


import com.kreait.bots.agile.domain.slack.standupDefinition.DialogOptionService
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.Callback
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.group.dialog.Dialog
import com.kreait.slack.api.contract.jackson.group.dialog.SelectElement
import com.kreait.slack.api.contract.jackson.group.dialog.SlackOpenDialogRequest
import com.kreait.slack.api.contract.jackson.group.dialog.TextAreaElement
import com.kreait.slack.api.contract.jackson.group.dialog.TextElement
import com.kreait.slack.api.contract.jackson.group.dialog.Type
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class CreateDialogOpeningService @Autowired constructor(private val slackClient: SlackClient,
                                                        private val message: MessageSourceAccessor,
                                                        private val dialogOptionService: DialogOptionService) {

    companion object {
        private const val CREATE_OPENING_TITLE = "createOpeningTitle"
        private const val NAME_PLACEHOLDER = "placeholderName"
        private const val DAYS_LABEL = "labelDays"
        private const val DAYS_PLACEHOLDER = "placeholderDays"
        private const val DAYS_HINT = "hintDays"
        private const val TIME_LABEL = "labelTime"
        private const val TIME_HINT = "hintTime"
        private const val BROADCAST_CHAN_LABEL = "labelBCC"
        private const val BROADCAST_HINT = "broadcastHint"
        private const val QUESTIONS_LABEL = "labelQuestions"
        private const val QUESTIONS_HINT = "hintQuestions"

        private val LOG = LoggerFactory.getLogger(CreateDialogOpeningService::class.java)
    }

    /**
     * Opens the creation dialog
     * @param triggerId trigger id of the event that triggered creation dialog opening
     */
    fun openCreationDialog(triggerId: String, userId: String, accessToken: String) {

        val userLocale = Locale.ENGLISH
        val openDialogRequest = SlackOpenDialogRequest(
                trigger_id = triggerId,
                dialog = Dialog(
                        callback_id = Callback.CREATION_DIALOG.id,
                        title = message.getMessage(CREATE_OPENING_TITLE, userLocale),
                        elements = listOf(
                                TextElement(
                                        label = "Name",
                                        name = CreateDialogSubmission.NAME,
                                        type = Type.TEXT,
                                        placeholder = message.getMessage(NAME_PLACEHOLDER, userLocale)
                                ),
                                TextElement(
                                        label = message.getMessage(DAYS_LABEL, userLocale),
                                        name = CreateDialogSubmission.DAYS,
                                        type = Type.TEXT,
                                        placeholder = message.getMessage(DAYS_PLACEHOLDER, userLocale),
                                        hint = message.getMessage(DAYS_HINT, userLocale)
                                ),
                                SelectElement(
                                        label = message.getMessage(TIME_LABEL, userLocale),
                                        name = CreateDialogSubmission.TIME,
                                        type = Type.SELECT,
                                        options = DialogOptionService.createTimeOptions(),
                                        hint = message.getMessage(TIME_HINT, userLocale)),
                                SelectElement(
                                        label = message.getMessage(BROADCAST_CHAN_LABEL, userLocale),
                                        name = CreateDialogSubmission.BROADCAST_CHANNEL_ID,
                                        type = Type.SELECT,
                                        hint = message.getMessage(BROADCAST_HINT),
                                        options = dialogOptionService.createChannelOptions(accessToken, userId)
                                ),
                                TextAreaElement(
                                        label = message.getMessage(QUESTIONS_LABEL, userLocale),
                                        name = CreateDialogSubmission.QUESTIONS,
                                        type = Type.TEXTAREA,
                                        hint = message.getMessage(QUESTIONS_HINT, userLocale)
                                )
                        )
                )
        )

        this.slackClient.dialog().open(accessToken)
                .with(openDialogRequest)
                .onFailure {
                    LOG.error("Failed to open creation dialog")
                }
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug("Successfully opened creation dialog")
                    }
                }
                .invoke()
    }
}
