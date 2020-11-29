package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.common.InstantSample
import com.kreait.slack.api.contract.jackson.common.types.Purpose
import com.kreait.slack.api.contract.jackson.common.types.Topic
import com.kreait.slack.api.contract.jackson.group.users.Channel
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class LeaveSlashCommandHandlerTest {

    @DisplayName("Test Leave command handler")
    @Test
    fun testLeaveCommandHandler() {
        val leaveDialogOpeningService = mock<LeaveDialogOpeningService>()
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } doReturn listOf(StandupDefinition.sample().copy(id = "sampleDefinition"))
        }
        val userService = mock<UserService> {
            //TODO create extension functions
            on { conversationList(any(), any()) } doReturn listOf(Channel("sample", "channel", false, false,
                    false, InstantSample.sample(), "", false, false, 0, "", false,
                    false, false, listOf(), false, false, false, false,
                    Topic("", "", InstantSample.sample()), Purpose("", "", InstantSample.sample()),
                    0, "", false))
        }
        val messageSender = mock<SlackMessageSender>()
        val command = SlackCommand.sample().copy(userId = "userId", teamId = "teamId")

        val leaveSlashCommandHandler = LeaveSlashCommandHandler(leaveDialogOpeningService, standupDefinitionRepository, userService, messageSender)
        leaveSlashCommandHandler.handleLeaveSlashCommand(eq(command), eq(""))
    }

    @DisplayName("Test Leave command handler")
    @Test
    fun testLeaveCommandHandlerWithoutStandups() {
        val leaveDialogOpeningService = mock<LeaveDialogOpeningService>()
        val standupDefinitionRepository = mock<StandupDefinitionRepository>()
        val messageSender = mock<SlackMessageSender>()
        val command = SlackCommand.sample().copy(userId = "userId", teamId = "teamId")

        val leaveSlashCommandHandler = LeaveSlashCommandHandler(leaveDialogOpeningService, standupDefinitionRepository, mock(), messageSender)
        leaveSlashCommandHandler.handleLeaveSlashCommand(command, "")
        verify(messageSender, times(1)).sendEphemeralMessage(eq(ResponseType.NO_STANDUPS_FOUND), any(), any(), eq(""))
    }

}