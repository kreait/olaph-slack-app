package com.kreait.bots.agile.domain.slack.standup.reminder

import com.kreait.bots.agile.domain.common.data.Standup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ReminderService @Autowired constructor(private val reminderMessageSender: ReminderMessageSender) {
    companion object {
        const val CONTINUE_BTN_KEY = "continueBtnText"
        const val SKIP_BTN_KEY = "skipBtnText"
        const val ATTACHMENT_FALLBACK = "attachmentFallback"
        const val REMINDER_CALLBACK = "ReminderCallback"

        const val CONTINUE_BUTTON_NAME = "reminderContinue"
        const val SKIP_BUTTON_NAME = "reminderSkip"
    }

    fun sendReminder(newStandup: Standup, oldStandup: Standup) {
        reminderMessageSender.sendReminderMessage(newStandup, oldStandup)
    }
}
