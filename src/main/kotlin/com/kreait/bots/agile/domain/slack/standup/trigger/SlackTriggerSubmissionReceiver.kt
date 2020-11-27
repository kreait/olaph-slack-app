package com.kreait.bots.agile.domain.slack.standup.trigger

import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class SlackTriggerSubmissionReceiver @Autowired constructor(private val triggerSubmissionHandler: TriggerSubmissionHandler) : InteractiveComponentReceiver<InteractiveMessage> {

    /**
     * receives the trigger-standup submission
     */
    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        triggerSubmissionHandler.handleSubmission(interactiveComponentResponse)
    }

    /**
     * checks if the submitted message belong to the trigger-standup dialog
     */
    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean {
        return interactiveComponentResponse.callbackId == TriggerStandupDialog.CALLBACK_ID
    }
}