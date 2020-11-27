package com.kreait.bots.agile.domain.slack.standupDefinition.edit.select

import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.bots.agile.domain.response.RandomResponseProvider
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

class PostEditStandupSelectionServiceTest {

    @DisplayName("")
    @Test
    fun testEditSelectionService() {
        val randomResponseProvider = mock<RandomResponseProvider>()
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } doReturn listOf(StandupDefinition.sample().copy(id = "sampleStandupDefinition", name = "sample"))
        }
        val userService = mock<UserService> {
            //TODO create extension function
            on { conversationList(any(), any()) } doReturn listOf(Channel("sample", "channel", false, false,
                    false, InstantSample.sample(), "", false, false, 0, "", false,
                    false, false, listOf(), false, false, false, false,
                    Topic("", "", InstantSample.sample()), Purpose("", "", InstantSample.sample()),
                    0, "", false))
        }
        val messageSender = mock<SlackMessageSender>()
        val postEditStandupSelectionService = PostEditStandupSelectionService(randomResponseProvider, standupDefinitionRepository, userService,
                messageSender)
        postEditStandupSelectionService.postSelectStandupQuestion(SlackCommand.sample(), "")
        verify(messageSender, times(1)).sendEphemeralMessage(responseType = eq(ResponseType.EDIT_INTRO), messageContext = any(), attachments = any(), token = eq(""))
    }
}