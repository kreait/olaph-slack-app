package com.kreait.bots.agile.core.standup.question

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.Standup.Status
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.standup.SlackQuestionSender
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

/**
 * Finds open standup questions and triggers sending in question Senders
 */
@Service
class QuestionService @Autowired constructor(
        private val standupRepository: StandupRepository,
        private val questionSender: SlackQuestionSender) {

    companion object {
        private val Log = LoggerFactory.getLogger(QuestionService::class.java)
    }

    /**
     * fetches [Standup]s to send questions
     * TODO this method doesnt send any questions, so please modify the name
     */
    fun sendQuestions(userId: String? = null) {
        val userIdfilter = if (userId != null) setOf(userId) else null

        this.standupRepository.find(withUserIds = userIdfilter, withStatus = setOf(Status.OPEN))
                .forEach {
                    sendQuestion(it)
                }
    }

    /**
     * sends a question to an user
     */
    private fun sendQuestion(standup: Standup) {
        try {
            if (Standup.Predicates.canSendQuestion(standup)) {
                if (questionSender.sendQuestion(standup.userId, standup.questions[standup.questionsAsked], standup.teamId)) {
                    this.standupRepository.update(withIds = setOf(standup.id!!), update = Update().inc(Standup.QUESTIONS_ASKED, 1))
                }
            }
        } catch (e: Exception) {
            Log.error("Error during standup question sending for $standup", e)
        }
    }

}
