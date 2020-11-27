package com.kreait.bots.agile.core.standupdefinition.create

import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalTime

@DisplayName("CreateDialogSubmission")
class CreateDialogSubmissionUnitTests {

    @Test
    @DisplayName("Deserialization")
    fun testDeserialization() {
        val payloadMap = mapOf(
                Pair(CreateDialogSubmission.NAME, "Name"),
                Pair(CreateDialogSubmission.DAYS, "mon tue wed"),
                Pair(CreateDialogSubmission.TIME, "10:00"),
                Pair(CreateDialogSubmission.BROADCAST_CHANNEL_ID, "ChannelId"),
                Pair(CreateDialogSubmission.QUESTIONS, "Q1\nQ2\nQ3")
        )
        val submission = CreateDialogSubmission.of(payloadMap)

        assertEquals(payloadMap[CreateDialogSubmission.NAME], submission.name)

        assertTrue(submission.days.size == 3)
        assertTrue(submission.days.containsAll(listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)))

        assertEquals(LocalTime.of(10, 0), submission.time)

        assertEquals(payloadMap[CreateDialogSubmission.BROADCAST_CHANNEL_ID], submission.broadcastChannelId)

        assertTrue(submission.questions.size == 3)
        assertTrue(submission.questions.containsAll(listOf("Q1", "Q2", "Q3")))
    }
}
