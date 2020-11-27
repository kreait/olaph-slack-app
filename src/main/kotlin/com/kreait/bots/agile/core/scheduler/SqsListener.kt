package com.kreait.bots.agile.core.scheduler

import com.fasterxml.jackson.annotation.JsonProperty
import com.kreait.bots.agile.core.standup.close.StandupCancelService
import com.kreait.bots.agile.core.standup.create.StandupCreationService
import com.kreait.bots.agile.core.standup.open.StandupOpeningService
import com.kreait.bots.agile.core.standup.question.QuestionService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.stereotype.Component

/**
 * the listener for incoming messages
 */
@Component
class SQSListener @Autowired constructor(private val standupOpeningService: StandupOpeningService,
                                         private val standupCreationService: StandupCreationService,
                                         private val questionService: QuestionService,
                                         private val standupCancelService: StandupCancelService) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SQSListener::class.java)
    }

    data class Type(@JsonProperty("Type") val type: String, @JsonProperty("Message") val message: Message)

    enum class Message {
        CREATE_STANUPS, OPEN_STANDUPS, SEND_QUESTIONS, CANCEL_QUESTIONS, TRIGGER_SCHEDULED_EVENTS
    }

    /**
     * onMessage-method which is triggered when a message is received
     * //TODO OLA-105
     * Creation, Opening, SendQuestions, Closing
     * @param message the message from the queue
     */
    @SqsListener(value = ["\${tasks.queue.name}"], deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    fun onMessage(@NotificationMessage message: Type?) {
        try {
            if (LOG.isDebugEnabled) {
                LOG.debug(message.toString())
            }
            when (message!!.message) {
                Message.CREATE_STANUPS -> {
                    this.standupCreationService.createStandupsFromStandupDefinitions()
                }
                Message.OPEN_STANDUPS -> {
                    this.standupOpeningService.findAndOpenStandups()
                }
                Message.SEND_QUESTIONS -> {
                    this.questionService.sendQuestions()
                }
                Message.CANCEL_QUESTIONS -> {
                    this.standupCancelService.cancelOutdatedStandups()
                }
            }

        } catch (e: Exception) {
            LOG.error("Error during standup lifecycle", e)
        }
    }
}
