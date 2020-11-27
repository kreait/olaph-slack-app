package com.kreait.bots.agile.domain.v2.listener

import com.kreait.bots.olaph.dto.jackson.queue.SqsEventMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.stereotype.Component

/**
 * Broadcasts incoming [SqsEventMessage]s to registered [MessageHandler]s
 */
@Component
class CommunicationServiceListener @Autowired constructor(private val messageHandlers: List<MessageHandler>) {

    companion object {
        val LOG = LoggerFactory.getLogger(CommunicationServiceListener::class.java)
    }

    @SqsListener(value = ["\${olaph.queueNames.communicationService}"], deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    fun onMessage(sqsEventMessage: SqsEventMessage) {
        LOG.debug(sqsEventMessage.toString())
        this.messageHandlers.filter { it.supportsMessage(sqsEventMessage) }.forEach { it.handle(sqsEventMessage) }
    }

}

/**
 * Receives incoming SQSMessages
 */
interface MessageHandler {

    /**
     * Whether the MessageHandler supports the message
     * @param actionMessage [SqsEventMessage]
     * @return true, if the MessageHandler supports the message
     */
    fun supportsMessage(actionMessage: SqsEventMessage): Boolean

    /**
     * Handles a [SqsEventMessage]
     * @param actionMessage [SqsEventMessage]
     */
    fun handle(actionMessage: SqsEventMessage)
}
