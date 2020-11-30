package com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.open

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.common.InstantSample
import com.kreait.slack.api.contract.jackson.common.types.Purpose
import com.kreait.slack.api.contract.jackson.common.types.Topic
import com.kreait.slack.api.contract.jackson.group.dialog.ErrorOpenDialogResponse
import com.kreait.slack.api.contract.jackson.group.dialog.MetaData
import com.kreait.slack.api.contract.jackson.group.dialog.SuccessfulOpenDialogResponse
import com.kreait.slack.api.contract.jackson.group.dialog.sample
import com.kreait.slack.api.contract.jackson.group.users.Channel
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.support.MessageSourceAccessor
import java.util.Locale

@UnitTest
class DeleteDialogOpeningServiceTests {

    private lateinit var userService: UserService
    val command = SlackCommand.sample().copy(teamId = "sampleTeam")

    @BeforeEach
    fun setup() {
        userService = mock {
            //TODO create extension
            on { conversationList(any(), any()) } doReturn listOf(Channel("sample", "channel", false, false,
                    false, InstantSample.sample(), "", false, false, 0, "", false,
                    false, false, listOf(), false, false, false, false,
                    Topic("", "", InstantSample.sample()), Purpose("", "", InstantSample.sample()),
                    0, "", false))
        }
    }

    private fun getDeleteDialogOpeningService(): DeleteDialogOpeningService {
        val slackClient = MockSlackClient()
        slackClient.dialog().open("").successResponse = SuccessfulOpenDialogResponse.sample()
        //TODO create ExtensionMethod
        slackClient.dialog().open("").failureResponse = ErrorOpenDialogResponse(false, "", MetaData(listOf("")))

        val message = mock<MessageSourceAccessor> {
            on { getMessage(any<String>(), any<Locale>()) } doReturn "sampleMessage"
        }
        return DeleteDialogOpeningService(slackClient, message)
    }

    @DisplayName("Test Successful DeleteSlashCommandHandler")
    @Test
    fun openDeletionDialog() {
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } doReturn listOf(StandupDefinition.sample().copy(id = "sampleId"))
        }
        val deleteSlashCommandHandler = DeleteSlashCommandHandler(getDeleteDialogOpeningService(), standupDefinitionRepository, userService, mock())

        deleteSlashCommandHandler.handleDeleteSlashCommand(command, "")
    }

    @DisplayName("Test Failure DeleteSlashCommandHandler")
    @Test
    fun testFailureDialog() {
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } doReturn listOf()
        }
        val slackMessageSender = mock<SlackMessageSender>()

        val deleteSlashCommandHandler = DeleteSlashCommandHandler(mock(), standupDefinitionRepository, userService, slackMessageSender)
        deleteSlashCommandHandler.handleDeleteSlashCommand(command, "")
        verify(slackMessageSender, times(1)).sendEphemeralMessage(responseType = ResponseType.NO_STANDUPS_FOUND, messageContext = MessageContext.of(command), token = "")
    }
}