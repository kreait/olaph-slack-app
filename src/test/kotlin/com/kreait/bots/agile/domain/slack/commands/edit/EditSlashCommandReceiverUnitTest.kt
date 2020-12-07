package com.kreait.bots.agile.domain.slack.commands.edit

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.slack.SlashCommandTest
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.EditSlashCommandReceiver
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.select.PostEditStandupSelectionService
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
class EditSlashCommandReceiverUnitTest : SlashCommandTest {

    @DisplayName("Test Supports Slash Command")
    @Test
    override fun supportsCommand() {
        val receiver = EditSlashCommandReceiver(mock { })

        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/edit-standup")))
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "edit")))
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "  edit ")))
        Assertions.assertFalse(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "edi")))
        Assertions.assertFalse(receiver.supportsCommand(SlackCommand.sample().copy(command = "sample")))
    }

    @DisplayName("Test Receive Slash Command")
    @Test
    override fun onReceiveSlashCommand() {
        val service = mock<PostEditStandupSelectionService> { }
        val receiver = EditSlashCommandReceiver(service)
        val slackCommand = SlackCommand.sample().copy(
                triggerId = "SampleTriggerID",
                teamId = "SampleTeamID",
                userId = "SampleUserID",
                channelId = "SampleChannelID",
                channelName = "SampleChannelName"
        )

        receiver.onReceiveSlashCommand(slackCommand, HttpHeaders.EMPTY, Team("", "",
                Team.Bot("", "")))
        verify(service, times(1)).postSelectStandupQuestion(slackCommand, "")
    }
}
