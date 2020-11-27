package com.kreait.bots.agile.domain.slack.commands.info

import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.slack.info.SlackSendInfoService
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.common.messaging.Color
import com.kreait.slack.api.contract.jackson.sample
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


class SlackSendInfoServiceUnitTests {


    @DisplayName("Successful")
    @Test
    fun successful() {
        val service = SlackSendInfoService(mock())

        service.sendInfoMessage(SlackCommand.sample(), "")
    }

    @DisplayName("Expected Attachments")
    @Test
    fun expectedAttachments() {

        val messageSender = mock<SlackMessageSender>()
        val service = SlackSendInfoService(messageSender)

        service.sendInfoMessage(SlackCommand.sample(), "")
        verify(messageSender, times(1)).sendCustomEphemeralMessage(attachments,
                MessageContext.of(SlackCommand.sample()), "", "")
    }

    val attachments = listOf(
            Attachment(
                    id = "",
                    title = "Create",
                    fallback = "",
                    pretext = "",
                    color = Color.GOOD,
                    text = "invoked with \"/create-standup\" or \"/olaph create\" \nOpens a Dialog to create a Stand-up"),
            Attachment(
                    id = "",
                    title = "Edit",
                    fallback = "",
                    pretext = "",
                    color = Color.WARNING,
                    text = "invoked with \"/edit-standup\" or \"/olaph edit\" \nOpens a Dialog to edit a Stand-up"),
            Attachment(
                    id = "",
                    title = "Delete",
                    fallback = "",
                    pretext = "",
                    color = Color.DANGER,
                    text = "invoked with \"/delete-standup\" or \"/olaph delete\" \nOpens a Dialog to delete a Stand-up"),
            Attachment(
                    id = "",
                    title = "Join",
                    fallback = "",
                    pretext = "",
                    color = Color.GOOD,
                    text = "invoked with \"/join-standup\" or \"/olaph join\" \nJoin a Stand-up you are eligible to join"),
            Attachment(
                    id = "",
                    title = "Leave",
                    fallback = "",
                    pretext = "",
                    color = Color.WARNING,
                    text = "invoked with \"/leave-standup\" or \"/olaph leave\" \nLeave a Stand-up you are currently part of"),
            Attachment(
                    id = "",
                    title = "Trigger",
                    fallback = "",
                    pretext = "",
                    color = Color.GOOD,
                    text = "invoked with \"/trigger-standup\" or \"/olaph trigger\" \nTriggers a Stand-up now if available"),
            Attachment(
                    id = "",
                    title = "Skip",
                    fallback = "",
                    pretext = "",
                    color = Color.WARNING,
                    text = "invoked with \"/skip-standup\" or \"/olaph skip\" \nSkip the current Stand-up"),
            Attachment(
                    id = "",
                    title = "List",
                    fallback = "",
                    pretext = "",
                    color = Color.GOOD,
                    text = "invoked with \"/list-standups\" or \"/olaph list\" \nLists the upcoming Stand-ups"),
            Attachment(
                    id = "",
                    title = "need more information?",
                    fallback = "",
                    pretext = "",
                    color = Color.NEUTRAL,
                    text = "visit https://olaph.io/ or send us an E-Mail to help@olaph.io")

    )
}
