package com.kreait.bots.agile.domain.slack.standupDefinition.list

import com.kreait.bots.agile.domain.slack.SlashCommandTest
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class ListCommandReceiverTest : SlashCommandTest {

    @DisplayName("Test List-Command Receiver")
    @Test
    override fun supportsCommand() {
        val receiver = ListCommandReceiver(mock())
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/list-standups")))
        Assertions.assertTrue(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "list")))

        Assertions.assertFalse(receiver.supportsCommand(SlackCommand.sample().copy(command = "/sample")))
        Assertions.assertFalse(receiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "lit")))
    }

    @DisplayName("Test List-Command Receiver")
    @Test
    override fun onReceiveSlashCommand() {
        val service = mock<ListStandupsService>()
        val receiver = ListCommandReceiver(service)
        val command = SlackCommand.sample().copy(command = "/list-standups")
        val olaphList = command.copy(command = "/olaph", text = "list")

        receiver.onReceiveSlashCommand(command, HttpHeaders.EMPTY, com.kreait.slack.broker.store.team.Team("", "",
                com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                com.kreait.slack.broker.store.team.Team.Bot("", "")))
        verify(service, times(1)).listStandups(command, "")

        receiver.onReceiveSlashCommand(olaphList, HttpHeaders.EMPTY, com.kreait.slack.broker.store.team.Team("", "",
                com.kreait.slack.broker.store.team.Team.IncomingWebhook("", "", "", ""),
                com.kreait.slack.broker.store.team.Team.Bot("", "")))
        verify(service, times(1)).listStandups(olaphList, "")
    }


}
