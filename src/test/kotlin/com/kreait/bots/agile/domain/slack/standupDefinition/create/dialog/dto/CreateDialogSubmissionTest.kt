package com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto

import com.kreait.bots.agile.UnitTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
internal class CreateDialogSubmissionTest {

    @DisplayName("Test DaysInput")
    @Test
    fun testInput() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            CreateDialogSubmission.of(getDialogSubmission())
        }
    }

    private fun getDialogSubmission(): Map<String, Any> {
        return mapOf<String, Any>(Pair(CreateDialogSubmission.NAME, "test1"),
                Pair(CreateDialogSubmission.DAYS, "mon tue wedij"),
                Pair(CreateDialogSubmission.TIME, "13:30"),
                Pair(CreateDialogSubmission.BROADCAST_CHANNEL_ID, "123"),
                Pair(CreateDialogSubmission.QUESTIONS, "what"),
                Pair(CreateDialogSubmission.STATE, ""))

    }
}
