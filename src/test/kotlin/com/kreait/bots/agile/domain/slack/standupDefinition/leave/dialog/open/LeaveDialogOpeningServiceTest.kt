package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.Action
import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.Callback
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.api.contract.jackson.group.dialog.Dialog
import com.kreait.slack.api.contract.jackson.group.dialog.Element
import com.kreait.slack.api.contract.jackson.group.dialog.Options
import com.kreait.slack.api.contract.jackson.group.dialog.SelectElement
import com.kreait.slack.api.contract.jackson.group.dialog.SlackOpenDialogRequest
import com.kreait.slack.api.contract.jackson.group.dialog.SuccessfulOpenDialogResponse
import com.kreait.slack.api.contract.jackson.group.dialog.Type
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.support.MessageSourceAccessor
import java.util.Locale

@UnitTest
class LeaveDialogOpeningServiceTest {

    @DisplayName("Test Leave-Dialog opening service")
    @Test
    fun testDialogService() {
        val slackClient = MockSlackClient()
        slackClient.dialog().open("").successResponse = SuccessfulOpenDialogResponse(true)

        val message = mock<MessageSourceAccessor> {
            on { getMessage(any<String>(), any<Locale>()) } doReturn "sampleMessage"
        }
        val leaveDialogOpeningService = LeaveDialogOpeningService(slackClient, message)

        val options = listOf(Options("label", "value"))
        leaveDialogOpeningService.openStandupLeaveDialog(options, "trigger_id", "user_id", "team_id")
        val expectedParam = SlackOpenDialogRequest(
                Dialog(
                        callback_id = Callback.LEAVE_DIALOG.id,
                        title = message.getMessage("titleLeave", Locale.ENGLISH),
                        elements = listOf<Element>(
                                SelectElement(
                                        name = Action.SELECTED_STANDUP.id,
                                        label = message.getMessage("labelLeave", Locale.ENGLISH),
                                        type = Type.SELECT,
                                        options = options)
                        )
                ), "trigger_id")
        Assertions.assertEquals(slackClient.dialog().open("").params(), expectedParam)

    }
}