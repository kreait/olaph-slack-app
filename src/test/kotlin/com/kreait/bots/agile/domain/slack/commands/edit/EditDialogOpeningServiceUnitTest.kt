package com.kreait.bots.agile.domain.slack.commands.edit

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.slack.standupDefinition.DialogOptionService
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.dialog.EditDialogOpeningService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.api.contract.jackson.group.dialog.Dialog
import com.kreait.slack.api.contract.jackson.group.dialog.ErrorOpenDialogResponse
import com.kreait.slack.api.contract.jackson.group.dialog.MetaData
import com.kreait.slack.api.contract.jackson.group.dialog.SelectElement
import com.kreait.slack.api.contract.jackson.group.dialog.SlackOpenDialogRequest
import com.kreait.slack.api.contract.jackson.group.dialog.SuccessfulOpenDialogResponse
import com.kreait.slack.api.contract.jackson.group.dialog.TextAreaElement
import com.kreait.slack.api.contract.jackson.group.dialog.TextElement
import com.kreait.slack.api.contract.jackson.group.dialog.Type
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Locale

@UnitTest
class EditDialogOpeningServiceUnitTest {

    @DisplayName("Failure EditDialog opening Test")
    @Test
    fun failure() {

        val mockSlackClient = MockSlackClient()
        mockSlackClient.dialog().open("token").failureResponse = ErrorOpenDialogResponse(false, "", MetaData(listOf()))
        val standupDefinition = StandupDefinition.sample()
        val service = EditDialogOpeningService(
                slackClient = mockSlackClient,
                message = mock { on { getMessage(any<String>(), any<Locale>()) } doReturn "" },
                standupDefinitionRepository = mock { on { findById(any(), any(), any()) } doReturn standupDefinition },
                dialogOptionService = mock { on { createChannelOptions(any(), any()) } doReturn listOf() }
        )
        val triggerId = "sampleTrigger"
        val userId = "sampleUser"
        val teamId = "sampleTeam"
        val standupDefinitionId = "sampleStandupDefinition"
        service.openEditDialog(triggerId, userId, teamId, standupDefinitionId, "")

        val expectedParam = SlackOpenDialogRequest(
                dialog = Dialog(
                        callback_id = "EDIT_DIALOG",
                        title = "Workspace",
                        elements = listOf(
                                TextElement(
                                        label = "Name",
                                        name = CreateDialogSubmission.NAME,
                                        type = Type.TEXT,
                                        placeholder = "",
                                        value = standupDefinition.name),
                                TextElement(
                                        label = "",
                                        name = CreateDialogSubmission.DAYS,
                                        type = Type.TEXT,
                                        placeholder = "",
                                        value = standupDefinition.days.joinToString(" ") { it.toString().subSequence(0, 3) },
                                        hint = ""),
                                SelectElement(
                                        label = "",
                                        name = CreateDialogSubmission.TIME,
                                        type = Type.SELECT,
                                        options = DialogOptionService.createTimeOptions(),
                                        value = "${standupDefinition.time}",
                                        hint = ""),
                                SelectElement(
                                        label = "",
                                        name = CreateDialogSubmission.BROADCAST_CHANNEL_ID,
                                        type = Type.SELECT,
                                        options = listOf(),
                                        hint = "",
                                        value = standupDefinition.broadcastChannelId),
                                TextAreaElement(
                                        label = "",
                                        name = CreateDialogSubmission.QUESTIONS,
                                        type = Type.TEXTAREA,
                                        value = standupDefinition.questions.joinToString("\n"),
                                        hint = "")
                        ), state = "sampleStandupDefinition"),
                trigger_id = "sampleTrigger")
        Assertions.assertEquals(expectedParam, mockSlackClient.dialog().open("token").params())
    }

    @DisplayName("Successfull EditDialog opening Test")
    @Test
    fun success() {

        val mockSlackClient = MockSlackClient()
        mockSlackClient.dialog().open("").successResponse = SuccessfulOpenDialogResponse(true)

        val standupDefinition = StandupDefinition.sample()
        val service = EditDialogOpeningService(
                slackClient = mockSlackClient,
                message = mock { on { getMessage(any<String>(), any<Locale>()) } doReturn "" },
                standupDefinitionRepository = mock { on { findById(any(), any(), any()) } doReturn standupDefinition },
                dialogOptionService = mock { on { createChannelOptions(any(), any()) } doReturn listOf() }
        )
        val triggerId = ""
        val userId = ""
        val teamId = ""
        val standupDefinitionId = ""
        service.openEditDialog(triggerId, userId, teamId, standupDefinitionId, "")
    }
}