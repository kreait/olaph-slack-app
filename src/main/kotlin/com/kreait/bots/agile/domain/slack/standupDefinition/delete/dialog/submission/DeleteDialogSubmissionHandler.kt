package com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.submission

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.Action
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class DeleteDialogSubmissionHandler(val slackClient: SlackClient,
                                    private val standupDefinitionRepository: StandupDefinitionRepository,
                                    private val standupRepository: StandupRepository,
                                    private val messageSender: SlackMessageSender) {
    companion object {
        private val LOG = LoggerFactory.getLogger(DeleteDialogSubmissionHandler::class.java)
    }

    fun handleDeleteDialogSubmission(interactiveComponentResponse: InteractiveMessage, accessToken: String) {
        val standup = standupDefinitionRepository.findById(
                interactiveComponentResponse.submission!![Action.SELECTED_STANDUP.id].toString(),
                interactiveComponentResponse.team.id
        )
        deleteStandup(standup.id!!)
        sendDeletionConfirmation(interactiveComponentResponse, standup.name, accessToken)
    }

    private fun deleteStandup(id: String) {
        this.standupDefinitionRepository.update(withId = id, withStatus = setOf(StandupDefinition.Status.ACTIVE),
                update = Update().set(StandupDefinition.STATUS, StandupDefinition.Status.ARCHIVED))
        this.standupRepository.update(withoutStatus = setOf(Standup.Status.CLOSED), withStandupDefinitionId = id, update = Update().set(Standup.STATUS, Standup.Status.CANCELLED))
    }

    fun sendDeletionConfirmation(interactiveComponentResponse: InteractiveMessage, standupName: String, accessToken: String) {
        //TODO if team is optional this cant work, if you see situation like this you should question if team can really be optional and if so there should be an appropiate way to handle the situation
        this.messageSender.sendEphemeralMessage(ResponseType.DELETE_SUCCESS,
                messageContext = MessageContext.of(interactiveComponentResponse).copy(currentStandup = standupName), token = accessToken)
    }
}
