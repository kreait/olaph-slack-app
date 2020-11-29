package com.kreait.bots.agile.domain.slack.standupDefinition.edit.select

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.slack.InteractiveComponentReceiverTest
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.Callback
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.dialog.EditDialogOpeningService
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.User
import com.kreait.slack.api.contract.jackson.common.Action
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

@UnitTest
class SelectStandupReceiverTest : InteractiveComponentReceiverTest {

    @DisplayName("")
    @Test
    override fun supportsInteractiveMessage() {
        val selectStandupReceiver = SelectStandupReceiver(mock())
        val component = InteractiveMessage.sample().copy(team = InteractiveComponentResponse.Team.sample().copy("sampleTeam"),
                user = User.sample().copy("sampleUser"),
                callbackId = Callback.EDIT_STANDUP_SELECTED.id)

        selectStandupReceiver.supportsInteractiveMessage(component)
    }

    @DisplayName("")
    @Test
    override fun onReceiveInteractiveMessage() {
        val editDialogOpeningService = mock<EditDialogOpeningService>()
        val selectStandupReceiver = SelectStandupReceiver(editDialogOpeningService)
        val component = InteractiveMessage.sample().copy(actions = listOf(Action(name = "sampleName", type = Action.ActionType.SELECT,
                selectedOptions = listOf(Action.Option("sampleOption", "standupDefinitionId")))),
                submission = mapOf(
                        Pair(CreateDialogSubmission.NAME, "Workspace"),
                        Pair(CreateDialogSubmission.DAYS, "mon"),
                        Pair(CreateDialogSubmission.TIME, "13:30"),
                        Pair(CreateDialogSubmission.BROADCAST_CHANNEL_ID, "channel"),
                        Pair(CreateDialogSubmission.QUESTIONS, "what")),
                team = InteractiveComponentResponse.Team.sample().copy("sampleTeam"), triggerId = "sampleTrigger",
                user = User.sample().copy("sampleUser"),
                callbackId = Callback.EDIT_STANDUP_SELECTED.id)
        selectStandupReceiver.onReceiveInteractiveMessage(component, HttpHeaders.EMPTY, com.kreait.slack.broker.store.team.Team("", "",
                com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                com.kreait.slack.broker.store.team.Team.Bot("", "")))
        verify(editDialogOpeningService, times(1)).openEditDialog("sampleTrigger", "sampleUser", "sampleTeam",
                "standupDefinitionId", "")

    }
}