package com.kreait.bots.agile.domain.v2.handler

import com.kreait.bots.agile.domain.v2.data.Questionnaire
import com.kreait.bots.agile.domain.v2.repository.QuestionnaireAnswerRepository
import com.kreait.bots.agile.domain.v2.repository.QuestionnaireRepository
import com.kreait.bots.agile.domain.v2.sample
import com.kreait.bots.agile.domain.v2.service.MessageBus
import com.kreait.bots.olaph.dto.jackson.queue.SendMessage
import com.kreait.bots.olaph.dto.jackson.queue.UserSentMessage
import com.kreait.bots.olaph.dto.jackson.queue.sample
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class UserSentMessageHandlerTest {
    private val userSentMessage = UserSentMessage.sample()

    private fun answerRepository() = mock<QuestionnaireAnswerRepository> {
        on { saveAnswer(any(), any()) } doReturn Questionnaire.sample().copy(id = "id",
                items = listOf(Questionnaire.Item.sample()))
    }

    @Test
    fun supportsMessage() {
        Assertions.assertTrue(UserSentMessageHandler(mock(), mock(), mock()).supportsMessage(userSentMessage))
    }

    @Test
    fun handleNotInStandup() {
        val messageBus = mock<MessageBus>()

        UserSentMessageHandler(messageBus,
                mock {
                    on {
                        findOneOrNull(withUserId = "", withLifeCycleStatus = Questionnaire.LifecycleStatus.OPEN)
                    } doReturn null
                },
                mock { }).handle(userSentMessage)

        verifySendMessagePayloadTypes(messageBus, SendMessage.Payload.NotInStandup::class.java)
    }


    @Test
    fun handleWithoutMissingAnswers() {
        val messageBus = mock<MessageBus>()

        val questionnaireRepository = mock<QuestionnaireRepository> {
            on {
                findOneOrNull(withUserId = "", withLifeCycleStatus = Questionnaire.LifecycleStatus.OPEN)
            } doReturn Questionnaire.sample().copy(id = "id")
        }

        UserSentMessageHandler(messageBus, questionnaireRepository, answerRepository()).handle(userSentMessage)

        verify(questionnaireRepository, times(1)).changeLifecycleStatus(any(), eq(Questionnaire.LifecycleStatus.CLOSED))
        verifySendMessagePayloadTypes(messageBus, SendMessage.Payload.Broadcast::class.java, SendMessage.Payload.AllQuestionsAnswered::class.java)
    }

    @Test
    fun handleWithMissingAnswers() {
        val messageBus = mock<MessageBus>()

        val questionnaireRepository = mock<QuestionnaireRepository> {
            on {
                findOneOrNull(withUserId = "", withLifeCycleStatus = Questionnaire.LifecycleStatus.OPEN)
            } doReturn Questionnaire.sample().copy(id = "id", items = listOf(
                    Questionnaire.Item.sample().copy(answer = null), Questionnaire.Item.sample().copy(answer = null)
            ))
        }

        UserSentMessageHandler(messageBus, questionnaireRepository, answerRepository()).handle(userSentMessage)

        verifySendMessagePayloadTypes(messageBus, SendMessage.Payload.SendQuestion::class.java)
    }

    private fun verifySendMessagePayloadTypes(messageBus: MessageBus, vararg payloadTypes: Class<out SendMessage.Payload>) {
        val argument = argumentCaptor<SendMessage>()
        verify(messageBus, times(payloadTypes.size)).publish(argument.capture())
        argument.allValues.forEach { assertThat(it.payload, anyOf(payloadTypes.map { instanceOf<SendMessage.Payload>(it) })) }
    }
}
