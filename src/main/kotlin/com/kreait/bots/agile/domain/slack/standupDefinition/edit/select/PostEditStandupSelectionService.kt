package com.kreait.bots.agile.domain.slack.standupDefinition.edit.select

import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.bots.agile.domain.response.RandomResponseProvider
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.Callback
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.common.Action
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.common.messaging.composition.Text
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PostEditStandupSelectionService(private val randomResponseProvider: RandomResponseProvider,
                                      private val standupDefinitionRepository: StandupDefinitionRepository,
                                      private val userService: UserService,
                                      private val slackMessageSender: SlackMessageSender) {

    companion object {
        private val LOG = LoggerFactory.getLogger(PostEditStandupSelectionService::class.java)
    }

    fun postSelectStandupQuestion(slackCommand: SlackCommand, accessToken: String) {

        val broadcastChannelList = userService.conversationList(accessToken, slackCommand.userId)

        val standupDefinitions = standupDefinitionRepository.find(
                withBroadcastChannels = broadcastChannelList.map { it.id }.toSet(),
                withTeamId = slackCommand.teamId,
                withStatus = setOf(StandupDefinition.Status.ACTIVE))

        if (standupDefinitions.isNotEmpty()) {
            val attachments = listOf(Attachment(
                    callbackId = Callback.EDIT_STANDUP_SELECTED.id,
                    attachmentType = "default",
                    fallback = "Select the stand-up you want to edit",
                    text = randomResponseProvider.getRandomizedResponse(ResponseType.SELECT_STANDUP_TO_EDIT),
                    actions = listOf(
                            Action(text = "Select a stand-up",
                                    name = "Standup",
                                    value = "",
                                    type = Action.ActionType.SELECT,
                                    options = standupDefinitions.map { Action.Option(text = it.name, value = it.id!!) })
                    )))
            slackMessageSender.sendEphemeralMessage(responseType = ResponseType.EDIT_INTRO,
                    attachments = attachments,
                    messageContext = MessageContext.of(slackCommand), token = accessToken)
        } else {
            slackMessageSender.sendEphemeralMessage(responseType = ResponseType.NO_STANDUPS_FOUND, messageContext = MessageContext.of(slackCommand), token = accessToken)
        }
    }
}
