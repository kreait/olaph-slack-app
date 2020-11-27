package com.kreait.bots.agile.domain.v2.handler

import com.kreait.bots.agile.domain.v2.data.Questionnaire
import com.kreait.bots.agile.domain.v2.listener.MessageHandler
import com.kreait.bots.agile.domain.v2.service.MessageBus
import com.kreait.bots.agile.domain.v2.repository.QuestionnaireRepository
import com.kreait.bots.agile.domain.v2.repository.QuestionnaireAnswerRepository
import com.kreait.bots.olaph.dto.jackson.common.Answer
import com.kreait.bots.olaph.dto.jackson.common.Item
import com.kreait.bots.olaph.dto.jackson.common.Question
import com.kreait.bots.olaph.dto.jackson.queue.SendMessage
import com.kreait.bots.olaph.dto.jackson.queue.SqsEventMessage
import com.kreait.bots.olaph.dto.jackson.queue.UserSentMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Handles messages sent by olaph users
 */
@Component
class UserSentMessageHandler @Autowired constructor(private val messageBus: MessageBus,
                                                    private val questionnaireRepository: QuestionnaireRepository,
                                                    private val answerRepository: QuestionnaireAnswerRepository) : MessageHandler {

    override fun supportsMessage(actionMessage: SqsEventMessage): Boolean = actionMessage is UserSentMessage

    /**
     * This method will...
     * - find an open questionnaire for the user
     * - if there is none, it will send a "not in standup" action
     * - if there is one it will save the users answer
     * - if there are more unanswered questions, it will send the next one
     * - if not, it will close the questionnaire, send the broadcast and an 'all questions answered' action
     */
    override fun handle(actionMessage: SqsEventMessage) {
        val m = actionMessage as UserSentMessage
        val userId = m.integrationUserId

        findOpenQuestionnaire(userId)
                ?.let { questionnaire ->
                    val currentItem = findCurrentItem(questionnaire)
                    val updatedQuestionnaire = saveAnswer(currentItem, m.text)
                    findNextUnansweredItem(currentItem, questionnaire)
                            ?.let { nextItem ->
                                sendNextQuestion(userId, nextItem.question)
                            }
                            ?: run {
                                closeQuestionnaire(updatedQuestionnaire)
                                sendBroadcast(updatedQuestionnaire)
                                sendAllQuestionsAnswered(userId)
                            }
                }
                ?: sendNotInStandup(userId)
    }

    private companion object {
        fun findCurrentItem(questionnaire: Questionnaire) = questionnaire.items.first { !it.isAnswered() }

        fun findNextUnansweredItem(currentQuestion: Questionnaire.Item, questionnaire: Questionnaire): Questionnaire.Item? {
            val nextQuestionIndex = questionnaire.items.indexOf(currentQuestion) + 1
            return questionnaire.items.getOrNull(nextQuestionIndex)?.takeIf { !it.isAnswered() }
        }
    }

    private fun findOpenQuestionnaire(userId: String) =
            this.questionnaireRepository.findOneOrNull(withUserId = userId, withLifeCycleStatus = Questionnaire.LifecycleStatus.OPEN)


    private fun saveAnswer(currentItem: Questionnaire.Item, answerValue: String): Questionnaire {
        return this.answerRepository.saveAnswer(currentItem.id, answerValue)
    }


    private fun sendNextQuestion(userId: String, question: Questionnaire.Item.Question) {
        this.messageBus.publish(SendMessage(payload =
        SendMessage.Payload.SendQuestion(userId = userId, question = Question(
                type = Question.Type.TEXT,
                text = question.text
        ))))
    }

    private fun sendNotInStandup(userId: String) {
        this.messageBus.publish(SendMessage(payload = SendMessage.Payload.NotInStandup(userId = userId)))
    }

    private fun sendBroadcast(questionnaire: Questionnaire) {
        questionnaire.broadcastChannelIds.forEach { broadcastChannelId ->
            this.messageBus.publish(SendMessage(payload = SendMessage.Payload.Broadcast(
                    channelId = broadcastChannelId,
                    userId = questionnaire.userId,
                    standupName = questionnaire.standupName,
                    date = questionnaire.date,
                    items = questionnaire.items.map {
                        Item(
                                Question(
                                        type = when (it.question.type) {
                                            Questionnaire.Item.Question.Type.TEXT -> Question.Type.TEXT
                                        },
                                        text = it.question.text),
                                Answer(
                                        value = it.answer!!.value))
                    }
            )))
        }
    }

    private fun sendAllQuestionsAnswered(userId: String) {
        this.messageBus.publish(SendMessage(payload = SendMessage.Payload.AllQuestionsAnswered(userId = userId)))
    }

    private fun closeQuestionnaire(questionnaire: Questionnaire) {
        this.questionnaireRepository.changeLifecycleStatus(questionnaire.id!!, Questionnaire.LifecycleStatus.CLOSED)
    }
}
