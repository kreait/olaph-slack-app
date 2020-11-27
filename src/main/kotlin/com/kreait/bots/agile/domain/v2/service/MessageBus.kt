package com.kreait.bots.agile.domain.v2.service

import com.kreait.bots.olaph.dto.jackson.queue.SqsActionMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.stereotype.Service

/**
 * Sends messages to the queue via sns topic
 */
@Service
class MessageBus @Autowired constructor(@Value("\${olaph.topicNames.standupService}") private val standupServiceTopicName: String,
                                        private val messagingTemplate: NotificationMessagingTemplate) {

    /**
     * Publishes a message to the SQS Queue
     * @param sqsActionMessage the message to be sent to sns
     */
    fun publish(sqsActionMessage: SqsActionMessage) {
        this.messagingTemplate.sendNotification(this.standupServiceTopicName,
                sqsActionMessage,
                null)
    }
}
