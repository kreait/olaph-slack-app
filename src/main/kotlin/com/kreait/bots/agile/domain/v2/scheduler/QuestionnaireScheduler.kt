package com.kreait.bots.agile.domain.v2.scheduler

import com.fasterxml.jackson.annotation.JsonProperty
import com.kreait.bots.agile.domain.v2.service.QuestionnaireCreationService
import com.kreait.bots.agile.domain.v2.service.QuestionnaireOpeningService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.stereotype.Component

/**
 * Listener for incoming messages from the scheduler
 */
@Component
class QuestionnaireScheduler @Autowired constructor(private val questionnaireCreationService: QuestionnaireCreationService,
                                                    private val questionnaireOpeningService: QuestionnaireOpeningService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(QuestionnaireScheduler::class.java)
    }

    data class SqsMessage(@JsonProperty("Type") val type: String, @JsonProperty("Message") val message: Message)

    enum class Message {
        CREATE_QUESTIONNAIRES,
        OPEN_QUESTIONNAIRES
    }

    /**
     * onMessage-method which is triggered when a message is received
     * @param message the message from the queue
     */
    @SqsListener(value = ["\${olaph.queueNames.scheduler}"], deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    fun onMessage(@NotificationMessage message: SqsMessage?) {
        try {
            if (LOG.isDebugEnabled) {
                LOG.debug(message.toString())
            }
            when (message!!.message) {
                Message.CREATE_QUESTIONNAIRES -> {
                    this.questionnaireCreationService.createQuestionnaires()
                }
                Message.OPEN_QUESTIONNAIRES -> {
                    this.questionnaireOpeningService.openQuestionnaires()
                }
            }
        } catch (e: Exception) {
            LOG.error("Error during standup lifecycle", e)
        }
    }

}
