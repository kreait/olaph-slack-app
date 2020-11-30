package com.kreait.bots.agile.domain.slack.commands.create

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.slack.SlashCommandTest
import com.kreait.bots.agile.domain.slack.standupDefinition.create.CreateSlashCommandReceiver
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.open.CreateDialogOpeningService
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

@UnitTest
class CreateSlashCommandReceiverUnitTest : SlashCommandTest {

    @DisplayName("Test Receive Slashcommand")
    @Test
    override fun onReceiveSlashCommand() {
        val service = mock<CreateDialogOpeningService> { }
        val receiver = CreateSlashCommandReceiver(service, mock { })
        val slackCommand = SlackCommand.sample().copy(triggerId = "SampleTriggerId", userId = "SampleUserId", teamId = "")
        receiver.onReceiveSlashCommand(slackCommand, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))

        verify(service, times(1)).openCreationDialog(slackCommand.triggerId, slackCommand.userId, slackCommand.teamId)
    }

    @DisplayName("Test Supports command")
    @Test
    override fun supportsCommand() {
        val receiver = CreateSlashCommandReceiver(mock { }, mock { })

        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/create-standup")))
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "create")))
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = " create   ")))
        Assertions.assertFalse(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "kreait")))
        Assertions.assertFalse(receiver.supportsCommand(SlackCommand.sample().copy(command = "/sample")))
    }
}
