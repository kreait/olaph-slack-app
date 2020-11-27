package com.kreait.bots.agile.domain.v2.service

import com.kreait.bots.agile.domain.v2.data.Questionnaire

import com.kreait.bots.agile.domain.v2.repository.QuestionnaireRepository
import com.kreait.bots.olaph.dto.jackson.common.Question
import com.kreait.bots.olaph.dto.jackson.queue.SendMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalTime


/**
 * Opens [Questionnaire]s
 */
@Service
class QuestionnaireOpeningService @Autowired constructor(private val questionnaireRepository: QuestionnaireRepository,
                                                         private val messageBus: MessageBus) {


    /**
     * Opens [Questionnaire]s
     * Gets all [Questionnaire]s that haven't been opened yet but that have their time set before the current system time,
     * grouped by user and sorted by time. If there is no open standup for a user already, it picks the first
     * [Questionnaire] per user, sets the status to OPEN and sends the first question
     */
    fun openQuestionnaires() {
        val grouped = questionnaireRepository.find(withBeforeTimestamp = Instant.now(),
                withLifeCycleStatus = Questionnaire.LifecycleStatus.CREATED)
                .sortedBy { it.time }
                .groupBy { it.userId } // group by user
                .filterNot { questionnaireRepository.exists(withUserId = it.key, withLifeCycleStatus = Questionnaire.LifecycleStatus.OPEN) } //Only proceed if there is no open standup for the user
                .map { it.value.first() } // pick the first standup per user
        grouped.forEach {
            questionnaireRepository.save(it.copy(lifecycleStatus = Questionnaire.LifecycleStatus.OPEN))
            this.messageBus.publish(
                    SendMessage(payload = SendMessage.Payload.FirstQuestion(
                            userId = it.userId,
                            standupName = it.standupName,
                            question = Question(Question.Type.TEXT, it.items.first().question.text)
                    ))
            )
        }
    }
}
