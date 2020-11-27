package com.kreait.bots.agile.domain.slack.standup.reminder

import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserChannelIdService
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.common.Action
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.support.MessageSourceAccessor

class ReminderMessageSenderTest {

    @DisplayName("Test RemindermessageSender")
    @Test
    fun testReminderMessageSender() {
        val userChannelIdService = mock<UserChannelIdService> {
            on { fetchChannelIdByUserId(any(), any()) } doReturn "sample"
        }
        val messageSender = mock<SlackMessageSender>()
        val message = mock<MessageSourceAccessor> {
            on { getMessage(any<String>()) } doReturn "sample"
        }
        val openStandup = Standup.sample().copy(name = "oldStandup")
        val standup = Standup.sample().copy(name = "newStandup")
        val reminder = ReminderMessageSender(userChannelIdService, messageSender, message, mock {
            on { findById(any()) } doReturn com.kreait.slack.broker.store.team.Team("", "",
                    com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                    com.kreait.slack.broker.store.team.Team.Bot("", ""))
        })
        reminder.sendReminderMessage(standup, openStandup)
        verify(messageSender, times(1)).sendMessage(
                ResponseType.REMINDER_MESSAGE,
                messageContext = MessageContext(currentStandup = openStandup.name, nextStandup = standup.name, userId = standup.userId,
                        teamId = standup.userId),
                attachments = listOf(Attachment(
                        actions = listOf(
                                Action(name = ReminderService.CONTINUE_BUTTON_NAME,
                                        text = message.getMessage(ReminderService.CONTINUE_BTN_KEY),
                                        style = Action.Style.PRIMARY,
                                        type = Action.ActionType.BUTTON),
                                Action(name = ReminderService.SKIP_BUTTON_NAME,
                                        text = message.getMessage(ReminderService.SKIP_BTN_KEY),
                                        style = Action.Style.DANGER,
                                        type = Action.ActionType.BUTTON)
                        ),
                        fallback = message.getMessage(ReminderService.ATTACHMENT_FALLBACK),
                        callbackId = ReminderService.REMINDER_CALLBACK,
                        attachmentType = "default")), token = "")
    }
}