package com.kreait.bots.agile.domain.slack.standupDefinition.edit.submission

import com.kreait.bots.agile.core.standup.create.StandupCreationService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.ConversationService
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto.CreateDialogSubmission
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.group.users.InfoRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

@Service
class EditDialogSubmissionHandler @Autowired constructor(private val slackClient: SlackClient,
                                                         private val standupDefinitionRepository: StandupDefinitionRepository,
                                                         private val conversationService: ConversationService,
                                                         private val standupRepository: StandupRepository,
                                                         private val standupCreationService: StandupCreationService,
                                                         private val messageSender: SlackMessageSender) {

    companion object {
        private val LOG = LoggerFactory.getLogger(EditDialogSubmissionHandler::class.java)
    }

    private fun buildUpdate(submission: CreateDialogSubmission, userIdList: List<String>, tzOffset: Int, userId: String): Update {

        return Update()
                .set(StandupDefinition.NAME, submission.name)
                .set(StandupDefinition.DAYS, submission.days)
                .set(StandupDefinition.TIME, submission.time)
                .set(StandupDefinition.BROADCAST_CHANNEL_ID, submission.broadcastChannelId)
                .set(StandupDefinition.SUBSCRIBED_USER_IDS, userIdList)
                .set(StandupDefinition.QUESTIONS, submission.questions)
                .set(StandupDefinition.TIMEZONE_OFFSET, tzOffset)
                .set(StandupDefinition.MODIFIED_AT, Instant.now())
                .set(StandupDefinition.MODIFIED_BY, userId)
    }

    fun handleEditDialogSubmission(submission: CreateDialogSubmission, interactiveComponentResponse: InteractiveMessage, token: String) {
        val state = interactiveComponentResponse.state!!
        this.slackClient.users()
                .info(token)
                .with(InfoRequest(interactiveComponentResponse.user.id, true))
                .onSuccess { userInfo ->

                    val standupDefinition = this.standupDefinitionRepository.findById(id = state, teamId = interactiveComponentResponse.team.id, status = setOf(StandupDefinition.Status.ACTIVE))
                    val members = if (standupDefinition.broadcastChannelId != submission.broadcastChannelId) {
                        conversationService.getNonBotMembersFromChannel(submission.broadcastChannelId, token)
                    } else {
                        standupDefinition.subscribedUserIds
                    }

                    userInfo.let { body ->
                        val offset = body.user.timezoneOffset ?: standupDefinition.offset
                        standupDefinitionRepository.update(withId = state, update = buildUpdate(submission, members, offset, interactiveComponentResponse.user.id))

                        updateStandups(state, submission.days)
                        this.messageSender.sendEphemeralMessage(ResponseType.EDIT_SUCCESS,
                                messageContext = MessageContext.of(interactiveComponentResponse).copy(currentStandup = submission.name), token = token)
                    }
                }

                .onFailure {
                    LOG.error("failed to retrieve user")
                }
                .invoke()

    }

    /**
     * deletes the [Standup.Status.CREATED] standups for this [standupDefinition] and recreates them
     */
    private fun updateStandups(standupDefinitionId: String, newDays: List<DayOfWeek>) {

        //TODO write delete with methode like update and find
        this.standupRepository.find(withStandupDefinitionId = standupDefinitionId, withStatus = setOf(Standup.Status.CREATED))
                .forEach {
                    this.standupRepository.delete(it)
                }
        if (newDays.contains(LocalDate.now().dayOfWeek)) {
            this.standupCreationService.createStandupsForStandupDefinition(standupDefinitionId)
        }
    }
}
