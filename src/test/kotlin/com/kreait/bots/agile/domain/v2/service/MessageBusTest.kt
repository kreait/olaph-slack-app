package com.kreait.bots.agile.domain.v2.service

import com.kreait.bots.olaph.dto.jackson.common.Question
import com.kreait.bots.olaph.dto.jackson.queue.SendMessage
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate

class MessageBusTest {

    @Test
    fun publish() {
        val template = mock<NotificationMessagingTemplate>()
        val messageBus = MessageBus("test-topic", template)
        val message = SendMessage(SendMessage.Payload.FirstQuestion("", "",
                Question(Question.Type.TEXT, "")))
        messageBus.publish(message)
        verify(template, times(1)).sendNotification("test-topic", message, null)
    }
}