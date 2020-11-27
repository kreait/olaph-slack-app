package com.kreait.bots.agile.domain.slack.standup.reminder.skip

import com.kreait.bots.agile.core.standup.open.StandupOpeningService
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.InteractiveComponentReceiverTest
import com.kreait.bots.agile.domain.slack.standup.reminder.ReminderService
import com.kreait.slack.api.contract.jackson.Channel
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.User
import com.kreait.slack.api.contract.jackson.common.Action
import com.kreait.slack.api.contract.jackson.common.InstantSample
import com.kreait.slack.api.contract.jackson.group.chat.ChatDeleteRequest
import com.kreait.slack.api.contract.jackson.group.chat.ErrorChatDeleteResponse
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulChatDeleteResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.broker.store.team.Team
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

class SkipSubmissionReceiverTest : InteractiveComponentReceiverTest {

    @DisplayName("test supportsInteractiveMessage")
    @Test
    override fun supportsInteractiveMessage() {
        val skipSubmissionReceiver = SkipSubmissionReceiver(mock(), mock(), mock())
        Assertions.assertTrue(skipSubmissionReceiver.supportsInteractiveMessage(InteractiveMessage.sample().copy(
                callbackId = ReminderService.REMINDER_CALLBACK,
                actions = listOf(Action(name = ReminderService.SKIP_BUTTON_NAME, type = Action.ActionType.BUTTON))
        )))
        Assertions.assertFalse(skipSubmissionReceiver.supportsInteractiveMessage(InteractiveMessage.sample().copy(
                callbackId = "sampleMessage",
                actions = listOf(Action(name = ReminderService.SKIP_BUTTON_NAME, type = Action.ActionType.BUTTON))
        )))
    }

    @DisplayName("test successful onReceiveInteractiveMessage")
    @Test
    override fun onReceiveInteractiveMessage() {
        val slackClient = MockSlackClient()
        val standupOpeningService = mock<StandupOpeningService>()
        val standupRepository = mock<StandupRepository>()

        slackClient.chat().delete("sampleToken").successResponse = SuccessfulChatDeleteResponse.sample()
        slackClient.chat().delete("sampleToken").failureResponse = ErrorChatDeleteResponse(true, "")
        val skipSubmissionReceiver = SkipSubmissionReceiver(standupRepository, standupOpeningService, slackClient)
        val expectedParam = ChatDeleteRequest.sample().copy(channel = "sampleChannel")
        skipSubmissionReceiver.onReceiveInteractiveMessage(InteractiveMessage.sample().copy(
                callbackId = ReminderService.REMINDER_CALLBACK,
                actions = listOf(Action(name = ReminderService.SKIP_BUTTON_NAME, type = Action.ActionType.BUTTON)),
                channel = Channel.sample().copy(id = "sampleChannel"),
                timestamp = InstantSample.sample(),
                team = InteractiveComponentResponse.Team.sample().copy("sampleTeam"),
                user = User.sample().copy("sampleUser")
        ), HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
        verify(standupOpeningService, times(1)).openStandup("sampleUser", "sampleTeam")
        Assertions.assertEquals(slackClient.chat().delete("").params(), expectedParam)
    }
}