package com.kreait.bots.agile.domain.slack.standup.skip

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.core.standup.open.StandupOpeningService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.SlashCommandTest
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

@UnitTest
class SlackSkipCommandReceiverTest : SlashCommandTest {

    @DisplayName("Test supports skip command")
    @Test
    override fun supportsCommand() {
        val slackSkipCommandReceiver = SlackSkipCommandReceiver(mock(), mock(), mock())
        slackSkipCommandReceiver.supportsCommand(SlackCommand.sample().copy(command = "/skip-standup"))
        slackSkipCommandReceiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = "skip"))
        slackSkipCommandReceiver.supportsCommand(SlackCommand.sample().copy(command = "/olaph", text = " skip "))
    }

    @DisplayName("Test Succesful receive skip command")
    @Test
    override fun onReceiveSlashCommand() {
        val slashCommand = SlackCommand.sample().copy(command = "/skip-standup", userId = "sampleUser", teamId = "sampleTeam")

        val standup = Standup.sample().copy(id = "sampleUser", name = "sampleName")
        val standupRepository = mock<StandupRepository> {
            on { find(withStatus = setOf(Standup.Status.OPEN), withUserIds = setOf("sampleUser")) } doReturn listOf(standup)
        }
        val skipMessageSender = mock<SkipMessageSender> {}
        val standupOpeningService = mock<StandupOpeningService>()

        val slackSkipCommandReceiver = SlackSkipCommandReceiver(standupRepository, skipMessageSender, standupOpeningService)
        slackSkipCommandReceiver.onReceiveSlashCommand(slashCommand, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))

        verify(skipMessageSender, times(1)).sendSuccesfulSkipMessage(slashCommand, standup.name, "")
        verify(standupOpeningService, times(1)).openStandup(slashCommand.userId, slashCommand.teamId)
    }

    @DisplayName("Test receive skip command without Standups")
    @Test
    fun onReceiveSlashCommandWithoutStandups() {
        val slashCommand = SlackCommand.sample().copy(command = "/skip-standup", userId = "sampleUser", teamId = "sampleTeam")

        val standupRepository = mock<StandupRepository> {
            on { find(withStatus = setOf(Standup.Status.OPEN), withUserIds = setOf("sampleUser")) } doReturn listOf()
        }
        val skipMessageSender = mock<SkipMessageSender> {}

        val slackSkipCommandReceiver = SlackSkipCommandReceiver(standupRepository, skipMessageSender, mock())
        slackSkipCommandReceiver.onReceiveSlashCommand(slashCommand, HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))

        verify(skipMessageSender, times(1)).sendNoStandupsFoundMessage(slashCommand, "")
    }

}
