package com.kreait.bots.agile.domain.slack.commands.info

import com.kreait.bots.agile.domain.slack.SlashCommandTest
import com.kreait.bots.agile.domain.slack.info.OlaphSlashCommandReceiver
import com.kreait.bots.agile.domain.slack.info.SlackSendInfoService
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

class OlaphSlashCommandReceiverUnitTests : SlashCommandTest {


    @DisplayName("SupportsCommand")
    @Test
    override fun supportsCommand() {

        val receiver = OlaphSlashCommandReceiver(mock { })
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph")))
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = " help")))
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "help")))
        Assertions.assertFalse(receiver.supportsCommand(SlackCommand.sample().copy(command = "/sample")))
    }

    @DisplayName("OnReceiveSlashCommend")
    @Test
    override fun onReceiveSlashCommand() {

        val service = mock<SlackSendInfoService> { }

        val receiver = OlaphSlashCommandReceiver(service)

        val slackCommand = SlackCommand.sample().copy(channelId = "SampleChannelId", userId = "SampleUserId", teamId = "")
        receiver.onReceiveSlashCommand(slackCommand, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))

        verify(service, times(1)).sendInfoMessage(slackCommand, "")
    }
}

