package com.kreait.bots.agile.domain.slack.standupDefinition.join

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.slack.SlashCommandTest
import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.open.JoinSlashCommandHandler
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.broker.store.team.Team
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

@UnitTest
class JoinSlashCommandReceiverTest : SlashCommandTest {

    @DisplayName("Test supports command")
    @Test
    override fun supportsCommand() {
        val joinSlashCommandReceiver = JoinSlashCommandReceiver(mock())
        joinSlashCommandReceiver.supportsCommand(SlackCommand.sample().copy(command = "/join-standup"))
        joinSlashCommandReceiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "join"))
        joinSlashCommandReceiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = " join"))
    }

    @DisplayName("Test receive command")
    @Test
    override fun onReceiveSlashCommand() {
        val handler = mock<JoinSlashCommandHandler>()
        val joinSlashCommandReceiver = JoinSlashCommandReceiver(handler)
        joinSlashCommandReceiver.onReceiveSlashCommand(
            SlackCommand.sample(), HttpHeaders.EMPTY, team = Team(
                "", "",
                Team.Bot("", "")
            )
        )
        verify(handler, times(1)).handleJoinSlashCommand(any(), eq(""))
    }
}
