package com.kreait.bots.agile.domain.v2.listener

import com.kreait.bots.agile.domain.v2.handler.UserSentMessageHandler
import com.kreait.bots.olaph.dto.jackson.queue.UserSentMessage
import com.kreait.bots.olaph.dto.jackson.queue.sample
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Test

class CommunicationServiceListenerTest {

    @Test
    fun onMessage() {
        val message = UserSentMessage.sample()
        val userSentMessageHandler = mock<UserSentMessageHandler> {
            on { supportsMessage(message) } doReturn true
        }
        val communicationServiceListener = CommunicationServiceListener(listOf(userSentMessageHandler))
        communicationServiceListener.onMessage(message)
        verify(userSentMessageHandler, times(1)).handle(message)
    }
}