package com.kreait.bots.agile.domain.slack.standup.reminder

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserChannelIdService
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.common.Action
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.broker.store.team.TeamStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service

@Service
class ReminderMessageSender @Autowired constructor(private val userChannelIdService: UserChannelIdService,
                                                   private val messageSender: SlackMessageSender,
                                                   private val message: MessageSourceAccessor,
                                                   private val teamStore: TeamStore) {
    companion object {
        private val Log = LoggerFactory.getLogger(ReminderMessageSender::class.java)
    }

    fun sendReminderMessage(standup: Standup, openStandup: Standup) {
        val token = teamStore.findById(standup.teamId).bot.accessToken
        val channelId = userChannelIdService.fetchChannelIdByUserId(userId = standup.userId, accessToken = token)
        val attachments = listOf(Attachment(
                actions = listOf(
                        Action(name = ReminderService.CONTINUE_BUTTON_NAME,
                                text = message.getMessage(ReminderService.CONTINUE_BTN_KEY),
                                style = Action.Style.PRIMARY,
                                type = Action.ActionType.BUTTON),
                        Action(name = ReminderService.SKIP_BUTTON_NAME,
                                text = message.getMessage(ReminderService.SKIP_BTN_KEY),
                                style = Action.Style.DANGER,
                                type = Action.ActionType.BUTTON)
                ), fallback = message.getMessage(ReminderService.ATTACHMENT_FALLBACK),
                callbackId = ReminderService.REMINDER_CALLBACK,
                attachmentType = "default"))

        channelId?.let {
            messageSender.sendMessage(ResponseType.REMINDER_MESSAGE,
                    messageContext = MessageContext(currentStandup = openStandup.name, nextStandup = standup.name, userId = standup.userId,
                            teamId = standup.teamId), attachments = attachments, token = token)
        }
    }
}