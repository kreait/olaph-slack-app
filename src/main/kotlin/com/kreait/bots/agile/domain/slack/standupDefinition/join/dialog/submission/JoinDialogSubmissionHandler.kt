package com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.submission

import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.Action
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JoinDialogSubmissionHandler(val slackClient: SlackClient,
                                  private val standupDefinitionRepository: StandupDefinitionRepository,
                                  private val messageSender: SlackMessageSender) {

    companion object {
        private val LOG = LoggerFactory.getLogger(JoinDialogSubmissionHandler::class.java)
    }


    fun handleJoinSubmission(interactiveComponentResponse: InteractiveMessage, accessToken: String) {
        val selectedStandupId = interactiveComponentResponse.submission!![Action.SELECTED_STANDUP.id].toString()
        val user = interactiveComponentResponse.user
        standupDefinitionRepository.addUserId(selectedStandupId, user.id)

        val standupId = interactiveComponentResponse.submission!![Action.SELECTED_STANDUP.id].toString()
        val teamId = interactiveComponentResponse.team.id
        val joinedStandup = standupDefinitionRepository.findById(standupId, teamId)

        joinedStandup.let {
            sendJoinConfirmation(interactiveComponentResponse, it.name, accessToken)
        }
    }

    private fun sendJoinConfirmation(interactiveComponentResponse: InteractiveMessage, standupName: String, accessToken: String) {
        this.messageSender.sendEphemeralMessage(responseType = ResponseType.SUCCESS_JOIN,
                messageContext = MessageContext.of(interactiveComponentResponse).copy(currentStandup = standupName),
                token = accessToken)
    }
}
