package com.kreait.bots.agile.domain.slack.commands.commands

import com.kreait.bots.agile.domain.slack.SlashCommandTest
import com.kreait.bots.agile.domain.slack.standup.trigger.SlackTriggerCommandReceiver
import com.kreait.bots.agile.domain.slack.standup.trigger.TriggerStandupDialog
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class TriggerStandupTest : SlashCommandTest {

    @DisplayName("Test supports trigger-command")
    @Test
    override fun supportsCommand() {
        val command = SlackCommand.sample().copy(command = "/trigger-standup")
        val olaphTrigger = command.copy(command = "/olaph", text = "trigger")
        val service = SlackTriggerCommandReceiver(mock { })
        service.supportsCommand(command)
        service.supportsCommand(olaphTrigger)
    }

    @DisplayName("Test receiver trigger-command")
    @Test
    override fun onReceiveSlashCommand() {
        val mockService = mock<TriggerStandupDialog> {}
        val service = SlackTriggerCommandReceiver(mockService)
        val command = SlackCommand.sample().copy(command = "/trigger-standup")
        service.onReceiveSlashCommand(command, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        verify(mockService, times(1)).openDialog(command, "")

        val olaphTrigger = command.copy(command = "/olaph", text = "trigger")
        service.onReceiveSlashCommand(olaphTrigger, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        verify(mockService, times(1)).openDialog(olaphTrigger, "")
    }
}