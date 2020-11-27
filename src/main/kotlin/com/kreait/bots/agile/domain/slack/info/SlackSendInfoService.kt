package com.kreait.bots.agile.domain.slack.info


import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.common.messaging.Color
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SlackSendInfoService @Autowired constructor(val messageSender: SlackMessageSender) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SlackSendInfoService::class.java)
        private val createDescription = Attachment("",
                "Create",
                "",
                "",
                color = Color.GOOD,
                text = "invoked with \"/create-standup\" or \"/olaph create\" \nOpens a Dialog to create a Stand-up")

        private val editDescription = Attachment("",
                "Edit",
                "",
                "",
                color = Color.WARNING,
                text = "invoked with \"/edit-standup\" or \"/olaph edit\" \nOpens a Dialog to edit a Stand-up")

        private val deleteDescription = Attachment("",
                "Delete",
                "",
                "",
                color = Color.DANGER,
                text = "invoked with \"/delete-standup\" or \"/olaph delete\" \nOpens a Dialog to delete a Stand-up")

        private val joinDescription = Attachment("",
                "Join",
                "",
                "",
                color = Color.GOOD,
                text = "invoked with \"/join-standup\" or \"/olaph join\" \nJoin a Stand-up you are eligible to join")

        private val leaveDescription = Attachment("",
                "Leave",
                "",
                "",
                color = Color.WARNING,
                text = "invoked with \"/leave-standup\" or \"/olaph leave\" \nLeave a Stand-up you are currently part of")

        private val triggerDescription = Attachment("",
                "Trigger",
                "",
                "",
                color = Color.GOOD,
                text = "invoked with \"/trigger-standup\" or \"/olaph trigger\" \nTriggers a Stand-up now if available")

        private val skipDescription = Attachment("",
                "Skip",
                "",
                "",
                color = Color.WARNING,
                text = "invoked with \"/skip-standup\" or \"/olaph skip\" \nSkip the current Stand-up")

        private val listDescription = Attachment("",
                title = "List",
                fallback = "",
                pretext = "",
                color = Color.GOOD,
                text = "invoked with \"/list-standups\" or \"/olaph list\" \nLists the upcoming Stand-ups")

        private val moreInfoDescription = Attachment("",
                "need more information?",
                "",
                "",
                color = Color.NEUTRAL,
                text = "visit https://olaph.io/ or send us an E-Mail to help@olaph.io")
    }

    /**
     * Sends a message to the user containing descriptions for all actions
     */
    fun sendInfoMessage(slashCommand: SlackCommand, token: String) {
        messageSender.sendCustomEphemeralMessage(listOf(
                createDescription,
                editDescription,
                deleteDescription,
                joinDescription,
                leaveDescription,
                triggerDescription,
                skipDescription,
                listDescription,
                moreInfoDescription), MessageContext.of(slashCommand), token, "")
    }
}
