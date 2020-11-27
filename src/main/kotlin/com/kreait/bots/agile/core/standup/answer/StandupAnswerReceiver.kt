package com.kreait.bots.agile.core.standup.answer

import com.kreait.bots.agile.core.standup.question.QuestionService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.RandomAnswerService
import com.kreait.bots.agile.domain.slack.standup.BroadcastingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class StandupAnswerReceiver @Autowired constructor(private val standupRepository: StandupRepository,
                                                   private val broadcastingService: BroadcastingService,
                                                   private val questionService: QuestionService,
                                                   private val randomAnswerService: RandomAnswerService) {

    /**
     * Handles incomming answers. Call this method in slack-agile-bot
     */
    fun handleAnswer(userId: String, answer: String, eventId: String, eventTime: Int, teamId: String) {
        if (Standup.Predicates(standupRepository).canSaveAnswer(userId = userId, answer = Standup.Answer(text = answer, eventId = eventId, eventTime = eventTime))) {
            if (this.standupRepository.exists(withStatus = setOf(Standup.Status.OPEN), withUserIds = setOf(userId))) {
                val standups = this.standupRepository.find(withStatus = setOf(Standup.Status.OPEN), withUserIds = setOf(userId))
                if (standups.size > 1) {
                    throw MultipleOpenStandupsException(standups)
                }
                val standup = standups[0]
                this.standupRepository.update(
                        withIds = setOf(standup.id!!),
                        update = Update().push(Standup.ANSWERS, Standup.Answer(text = answer, eventId = eventId, eventTime = eventTime))
                )
                this.questionService.sendQuestions(standup.userId)

                this.broadcastingService.broadcast(userId = standup.userId)
            } else {
                randomAnswerService.handleMessage(userId, answer, teamId)
            }
        }
    }
}

/**
 * It is not possible that there are more than 1 standups open
 */
class MultipleOpenStandupsException(standups: List<Standup>) : RuntimeException(standups.joinToString { ", " })
