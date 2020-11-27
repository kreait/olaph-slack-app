package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.Action
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service


@Service
class LeaveCommandSubmissionHandler @Autowired constructor(private val standupDefinitionRepository: StandupDefinitionRepository,
                                                           private val standupRepository: StandupRepository,
                                                           private val messageSender: SlackMessageSender) {
    companion object {
        private val Log = LoggerFactory.getLogger(LeaveCommandSubmissionHandler::class.java)
    }


    /**
     * handles the submitted data of the leave-dialog
     */
    fun handleSubmission(interactiveComponentResponse: InteractiveMessage, accessToken: String) {
        interactiveComponentResponse.submission?.let { response ->
            val teamId = interactiveComponentResponse.team.id
            val userId = interactiveComponentResponse.user.id
            val standupId = response[Action.SELECTED_STANDUP.id].toString()
            this.standupDefinitionRepository.find(withTeamId = teamId, withUserId = userId)
                    .forEach { standupDefinition ->
                        if (standupDefinition.id == standupId) {
                            this.standupDefinitionRepository.save(standupDefinition.copy(subscribedUserIds = standupDefinition.subscribedUserIds.minus(userId)))
                            closeAllStandups(userId, standupDefinition.id)
                            sendConfirmation(interactiveComponentResponse, standupDefinition.name, accessToken)
                        }
                    }
            response[Action.SELECTED_STANDUP.name].toString()
        }
    }

    /**
     * sends a confirmation-message to the user
     * if the user executed the command in a dm channel, the message will be posted to the conversation with the bot user
     */
    private fun sendConfirmation(interactiveComponentResponse: InteractiveMessage, standupName: String, accessToken: String) {
        this.messageSender.sendEphemeralMessage(ResponseType.LEAVE_CONFIRMATION,
                messageContext = MessageContext.of(interactiveComponentResponse).copy(currentStandup = standupName),
                token = accessToken)
    }

    private fun closeAllStandups(userId: String, id: String) {
        this.standupRepository.update(withUserIds = setOf(userId), withStandupDefinitionId = id, update = Update.update(Standup.STATUS, Standup.Status.CANCELLED))
    }
}
