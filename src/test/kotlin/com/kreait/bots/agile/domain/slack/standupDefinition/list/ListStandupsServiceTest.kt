package com.kreait.bots.agile.domain.slack.standupDefinition.list

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.core.standupdefinition.sample
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.common.InstantSample
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.common.messaging.Color
import com.kreait.slack.api.contract.jackson.common.types.Purpose
import com.kreait.slack.api.contract.jackson.common.types.Topic
import com.kreait.slack.api.contract.jackson.group.users.Channel
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@UnitTest
class ListStandupsServiceTest {

    @DisplayName("Test List Standups service")
    @Test
    fun testListStandupsService() {
        val standup = StandupDefinition.sample()
        val standupDefinitionRepository = mock<StandupDefinitionRepository> {
            on {
                find(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
            } doReturn listOf(standup, standup)
        }
        val messageSender = mock<SlackMessageSender>()

        val userService = mock<UserService> {
            on {
                conversationList(any(), any())
            } doReturn listOf(Channel("sample", "channel", false, false,
                    false, InstantSample.sample() , "", false, false, 0, "", false,
                    false, false, listOf(), false, false, false, false,
                    Topic("", "", InstantSample.sample()), Purpose("", "", InstantSample.sample()),
                    0, "", false))
        }
        val listStandupsService = ListStandupsService(standupDefinitionRepository, messageSender, userService)
        val command = SlackCommand.sample()
        listStandupsService.listStandups(command, "")

        verify(messageSender, times(1)).sendCustomEphemeralMessage(listOf(Attachment(
                title = standup.name, text = "The next one is ${DayHelper.getNextStandupDay(standup.days, standup.time, standup.offset.toLong(), LocalDateTime.now())} at ${standup.time}",
                color = if (standup.subscribedUserIds.contains(command.userId)) Color.GOOD else Color.NEUTRAL,
                fallback = "standup ${standup.name}"),
                Attachment(
                        title = standup.name, text = "The next one is ${DayHelper.getNextStandupDay(standup.days, standup.time, standup.offset.toLong(), LocalDateTime.now())} at ${standup.time}",
                        color = if (standup.subscribedUserIds.contains(command.userId)) Color.GOOD else Color.NEUTRAL,
                        fallback = "standup ${standup.name}")
        ), MessageContext.of(command), "", "These are your stand-ups: ")
    }
}