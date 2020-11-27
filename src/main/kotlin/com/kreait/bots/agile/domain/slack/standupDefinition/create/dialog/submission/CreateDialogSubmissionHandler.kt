package com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.submission

import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.ConversationService
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.common.messaging.Color
import com.kreait.slack.api.contract.jackson.group.users.InfoRequest
import com.kreait.slack.broker.store.team.Team
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class CreateDialogSubmissionHandler @Autowired constructor(private val slackClient: SlackClient,
                                                           private val standupDefinitionRepository: StandupDefinitionRepository,
                                                           private val conversationService: ConversationService,
                                                           private val messageSender: SlackMessageSender) {

    companion object {
        private fun createStandupDefinitionFromSubmission(userId: String, definitionSubmission: CreateDialogSubmission, userIdList: List<String>, teamId: String, timeZoneOffset: Int): StandupDefinition {
            return StandupDefinition(
                    name = definitionSubmission.name,
                    days = definitionSubmission.days,
                    time = definitionSubmission.time,
                    offset = timeZoneOffset,
                    broadcastChannelId = definitionSubmission.broadcastChannelId,
                    subscribedUserIds = userIdList,
                    questions = definitionSubmission.questions,
                    createdBy = userId,
                    createdAt = Instant.now(),
                    teamId = teamId)
        }

        private val LOG = LoggerFactory.getLogger(CreateDialogSubmissionHandler::class.java)
    }

    /**
     * Persists a Standupdefinition to the database. Then sends a confirmation message
     * @param definitionSubmission standup definitionSubmission object
     * @param responseChannelId channel id from where the request was triggered
     */
    fun handleCreateDialogSubmission(definitionSubmission: CreateDialogSubmission, interactiveComponentResponse: InteractiveMessage, team: Team) {
        val teamId = interactiveComponentResponse.team.id
        val userId = interactiveComponentResponse.user.id
        this.slackClient.users()
                .info(team.bot.accessToken)
                .with(InfoRequest(userId, true))
                .onSuccess {
                    var offset = it.user.timezoneOffset
                    if (offset == null)
                        offset = 0
                    val persistedStandup = this.standupDefinitionRepository.insert(createStandupDefinitionFromSubmission(
                            userId,
                            definitionSubmission,
                            conversationService.getNonBotMembersFromChannel(definitionSubmission.broadcastChannelId, team.bot.accessToken),
                            teamId, offset))

                    this.messageSender.sendEphemeralMessage(ResponseType.CREATION_SUCCESS,
                            listOf(Attachment(title = persistedStandup.name, fallback = "Standup defined :)", color = Color.GOOD)),
                            MessageContext.of(interactiveComponentResponse).copy(currentStandup = persistedStandup.name), team.bot.accessToken)
                }
                .onFailure {
                    LOG.error("Failed to handle create-dialog submission ({})", it::class.java)
                }
                .invoke()
    }
}
