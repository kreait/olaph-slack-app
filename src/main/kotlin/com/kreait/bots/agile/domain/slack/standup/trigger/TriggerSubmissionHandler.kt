package com.kreait.bots.agile.domain.slack.standup.trigger

import com.kreait.bots.agile.core.standup.create.StandupCreationService
import com.kreait.bots.agile.core.standup.question.QuestionService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.standup.SlackOpeningMessageSender
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TriggerSubmissionHandler @Autowired constructor(private val standupDefinitionRepository: StandupDefinitionRepository,
                                                      private val standupRepository: StandupRepository,
                                                      private val standupCreationService: StandupCreationService,
                                                      private val slackOpeningMessageSender: SlackOpeningMessageSender,
                                                      private val questionService: QuestionService) {

    companion object {
        private val Log = LoggerFactory.getLogger(TriggerSubmissionHandler::class.java)
    }

    /**
     * handles the trigger standup submission
     */
    fun handleSubmission(interactiveComponentResponse: InteractiveMessage) {
        Log.debug(interactiveComponentResponse.toString())

        openStandup(interactiveComponentResponse.user.id,
                interactiveComponentResponse.submission?.get(TriggerStandupDialog.STANDUP_SELECTION).toString(),
                interactiveComponentResponse.team.id)
    }

    /**
     * opens a standup and sends first message
     */
    private fun openStandup(userId: String, standupDefinitionId: String, teamId: String) {
        val standupDefinition = standupDefinitionRepository.findById(id = standupDefinitionId,
                teamId = teamId,
                status = setOf(StandupDefinition.Status.ACTIVE))

        if (!this.standupRepository.exists(withUserIds = setOf(userId),
                        withStatus = setOf(Standup.Status.CREATED),
                        isOnDate = LocalDate.now(),
                        withStandupDefinitionId = standupDefinitionId)) {
            this.standupCreationService.createStandupsForStandupDefinition(standupDefinition)
        }

        this.standupRepository.update(
                withStandupDefinitionId = standupDefinitionId,
                withUserIds = setOf(userId),
                withStatus = setOf(Standup.Status.CREATED),
                isOnDate = LocalDate.now(),
                update = Update.update(Standup.STATUS, Standup.Status.OPEN))

        val standup = this.standupRepository.find(withStandupDefinitionId = standupDefinitionId,
                withUserIds = setOf(userId),
                withStatus = setOf(Standup.Status.OPEN),
                isOnDate = LocalDate.now()).firstOrNull()
        if (standup != null) {
            this.slackOpeningMessageSender.sendOpeningMessage(standup)
            this.questionService.sendQuestions(userId)
        }
    }
}
