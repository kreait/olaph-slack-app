package com.kreait.bots.agile.domain.slack.standupDefinition.edit.dialog

import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.slack.standupDefinition.DialogOptionService
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.Callback
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
class EditDialogOpeningService @Autowired constructor(private val slackClient: SlackClient,
                                                      private val message: MessageSourceAccessor,
                                                      private val standupDefinitionRepository: StandupDefinitionRepository,
                                                      private val dialogOptionService: DialogOptionService) {

    companion object {
        private const val NAME_PLACEHOLDER = "placeholderName"
        private const val DAYS_LABEL = "labelDays"
        private const val DAYS_PLACEHOLDER = "placeholderDays"
        private const val DAYS_HINT = "hintDays"
        private const val TIME_LABEL = "labelTimeEdit"
        private const val BROADCAST_CHAN_LABEL = "labelBCC"
        private const val QUESTIONS_LABEL = "labelQuestions"
        private const val QUESTIONS_HINT = "hintQuestions"
        private const val TIME_HINT = "hintTime"
        private const val BROADCAST_HINT = "broadcastHint"


        private val LOG = LoggerFactory.getLogger(EditDialogOpeningService::class.java)
    }

    /**
     * Opens the creation dialog
     * @param triggerId trigger id of the event that triggered creation dialog opening
     */
    fun openEditDialog(triggerId: String, userId: String, teamId: String, standupDefinitionId: String, accessToken: String) {

        val userLocale = Locale.ENGLISH

        val standupDefinition = standupDefinitionRepository.findById(standupDefinitionId, teamId)

        val timezoneString = if (standupDefinition.offset > 0) {
            "+ ${standupDefinition.offset.div(60).div(60)}"
        } else {
            standupDefinition.offset.div(60).div(60).toString()
        }

        val req = SlackOpenDialogRequest(
                trigger_id = triggerId,
                dialog = Dialog(
                        state = standupDefinitionId,
                        callback_id = Callback.EDIT_DIALOG.id,
                        title = if (standupDefinition.name.length > 24) "${standupDefinition.name.substring(0, 21)}..." else standupDefinition.name,
                        elements = listOf(
                                TextElement(label = "Name",
                                        name = CreateDialogSubmission.NAME,
                                        type = Type.TEXT,
                                        placeholder = message.getMessage(NAME_PLACEHOLDER, userLocale),
                                        value = standupDefinition.name),
                                TextElement(
                                        label = message.getMessage(DAYS_LABEL, userLocale),
                                        name = CreateDialogSubmission.DAYS,
                                        type = Type.TEXT,
                                        placeholder = message.getMessage(DAYS_PLACEHOLDER, userLocale),
                                        value = standupDefinition.days.joinToString(" ") { it.toString().subSequence(0, 3) },
                                        hint = message.getMessage(DAYS_HINT, userLocale)),
                                SelectElement(
                                        label = String.format(message.getMessage(TIME_LABEL, userLocale), timezoneString),
                                        name = CreateDialogSubmission.TIME,
                                        type = Type.SELECT,
                                        options = DialogOptionService.createTimeOptions(),
                                        value = "${standupDefinition.time}",
                                        hint = message.getMessage(TIME_HINT, userLocale)),
                                SelectElement(
                                        label = message.getMessage(BROADCAST_CHAN_LABEL, userLocale),
                                        name = CreateDialogSubmission.BROADCAST_CHANNEL_ID,
                                        type = Type.SELECT,
                                        hint = message.getMessage(BROADCAST_HINT, userLocale),
                                        options = dialogOptionService.createChannelOptions(accessToken, userId),
                                        value = standupDefinition.broadcastChannelId),
                                TextAreaElement(
                                        label = message.getMessage(QUESTIONS_LABEL, userLocale),
                                        name = CreateDialogSubmission.QUESTIONS,
                                        type = Type.TEXTAREA,
                                        value = standupDefinition.questions.joinToString("\n"),
                                        hint = message.getMessage(QUESTIONS_HINT, userLocale)))
                ))

        this.slackClient.dialog().open(accessToken)
                .with(req)
                .onFailure {
                    LOG.error("Failed to open edit-dialog")
                }
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug("Successfully opened edit-dialog")
                    }
                }
                .invoke()
    }
}
