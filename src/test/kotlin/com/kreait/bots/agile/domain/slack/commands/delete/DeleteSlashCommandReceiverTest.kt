package com.kreait.bots.agile.domain.slack.commands.delete

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.slack.SlashCommandTest
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.DeleteSlashCommandReceiver
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.open.DeleteSlashCommandHandler
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.help.SlackDeletionHelpMessageService
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
class DeleteSlashCommandReceiverTest : SlashCommandTest {

    @DisplayName("Test supports delete-command")
    @Test
    override fun supportsCommand() {
        val service = DeleteSlashCommandReceiver(mock(), mock())
        Assertions.assertTrue(service.supportsCommand(slackCommand = SlackCommand.sample().copy(command = "/delete-standup")))
        Assertions.assertTrue(service.supportsCommand(slackCommand = SlackCommand.sample().copy(command = "/olaph", text = "delete")))
        Assertions.assertTrue(service.supportsCommand(slackCommand = SlackCommand.sample().copy(command = "/olaph", text = " delete")))
        Assertions.assertFalse(service.supportsCommand(slackCommand = SlackCommand.sample().copy(command = "/olaph", text = "delta")))
        Assertions.assertFalse(service.supportsCommand(slackCommand = SlackCommand.sample().copy(command = "/sample")))
    }

    @DisplayName("Test receive delete-command")
    @Test
    override fun onReceiveSlashCommand() {

        val deleteSlashCommandHandler = mock<DeleteSlashCommandHandler>()
        val slackDeletionHelpMessageService = mock<SlackDeletionHelpMessageService>()
        val service = DeleteSlashCommandReceiver(deleteSlashCommandHandler, slackDeletionHelpMessageService)

        val command = SlackCommand.sample().copy(command = "/delete-standup")
        service.onReceiveSlashCommand(command, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        verify(deleteSlashCommandHandler, times(1)).handleDeleteSlashCommand(command, "")

        var olaphDelete = command.copy(command = "/olaph", text = "delete")
        service.onReceiveSlashCommand(olaphDelete, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        verify(deleteSlashCommandHandler, times(1)).handleDeleteSlashCommand(olaphDelete, "")


        val helpCommand = command.copy(text = "help")
        service.onReceiveSlashCommand(helpCommand, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        verify(slackDeletionHelpMessageService, times(1)).sendHelpMessage(helpCommand, "")
    }

}
