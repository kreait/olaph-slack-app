package com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.open

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserService
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.group.dialog.Options
import com.kreait.slack.api.contract.jackson.sample
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class JoinSlashCommandHandlerTest {

    @DisplayName("test Join command handler")
    @Test
    fun testJoinHandler() {
        val joinDialogOpeningService = mock<JoinDialogOpeningService>()
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } doReturn listOf(StandupDefinition.sample().copy(id = "sampleStandupDefinition"))
        }
        val userService = mock<UserService>()
        val messageSender = mock<SlackMessageSender>()

        val joinSlashCommandHandler = JoinSlashCommandHandler(joinDialogOpeningService, standupDefinitionRepository, userService, messageSender)

        val command = SlackCommand.sample().copy(userId = "sampleUser", teamId = "")
        joinSlashCommandHandler.handleJoinSlashCommand(command, "")
        verify(joinDialogOpeningService, times(1)).openStandupJoinDialog(listOf(Options("Workspace", "sampleStandupDefinition")),
                command.triggerId, command.userId, command.teamId)
    }
}