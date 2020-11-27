package com.kreait.bots.agile.domain.slack.standup.reminder.continue_standup

import com.kreait.bots.agile.domain.slack.InteractiveComponentReceiverTest
import com.kreait.bots.agile.domain.slack.standup.reminder.ReminderService
import com.kreait.slack.api.contract.jackson.Channel
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.User
import com.kreait.slack.api.contract.jackson.common.Action
import com.kreait.slack.api.contract.jackson.group.chat.ChatDeleteRequest
import com.kreait.slack.api.contract.jackson.group.chat.ErrorChatDeleteResponse
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulChatDeleteResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.broker.store.team.Team
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders

@DisplayName("Test Continue Submission")
class ContinueSubmissionReceiverTest : InteractiveComponentReceiverTest {

    @DisplayName("test supportsInteractiveMessage")
    @Test
    override fun supportsInteractiveMessage() {
        val receiver = ContinueSubmissionReceiver(mock())
        receiver.supportsInteractiveMessage(InteractiveMessage.sample().copy(
                actions = listOf(Action(name = ReminderService.CONTINUE_BUTTON_NAME, type = Action.ActionType.BUTTON)),
                callbackId = ReminderService.REMINDER_CALLBACK, channel = Channel.sample(),
                team = InteractiveComponentResponse.Team.sample(), token = "", triggerId = "",
                user = User.sample()))
    }

    @DisplayName("test successful onReceiveInteractiveMessage")
    @Test
    override fun onReceiveInteractiveMessage() {
        val slackClient = MockSlackClient()

        val receiver = ContinueSubmissionReceiver(slackClient)
        slackClient.chat().delete("").successResponse = SuccessfulChatDeleteResponse.sample()

        val expectedParam = ChatDeleteRequest.sample()
        receiver.onReceiveInteractiveMessage(InteractiveMessage.sample().copy(
                actions = listOf(Action(name = ReminderService.CONTINUE_BUTTON_NAME, type = Action.ActionType.BUTTON)),
                callbackId = ReminderService.REMINDER_CALLBACK,
                channel = Channel.sample().copy(id = ""),
                team = InteractiveComponentResponse.Team.sample().copy(""),
                user = User.sample()), HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("ChannelId", "", "", ""),
                Team.Bot("", "")))
        Assertions.assertEquals(slackClient.chat().delete("").params(), expectedParam)
    }

    @DisplayName("test failure onReceiveInteractiveMessage")
    @Test
    fun onReceiveInteractiveMessageFailure() {
        val slackClient = MockSlackClient()

        val receiver = ContinueSubmissionReceiver(slackClient)
        slackClient.chat().delete("").failureResponse = ErrorChatDeleteResponse.sample()

        receiver.onReceiveInteractiveMessage(InteractiveMessage.sample().copy(
                actions = listOf(Action(name = ReminderService.CONTINUE_BUTTON_NAME, type = Action.ActionType.BUTTON)),
                callbackId = ReminderService.REMINDER_CALLBACK, channel = Channel("sampleChannel", ""),
                team = InteractiveComponentResponse.Team.sample().copy("sampleTeam"),
                user = User.sample()), HttpHeaders.EMPTY, Team("", "",
                Team.IncomingWebhook("", "", "", ""),
                Team.Bot("", "")))
    }

}