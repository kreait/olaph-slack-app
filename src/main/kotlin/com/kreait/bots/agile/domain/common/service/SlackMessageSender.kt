package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.domain.response.RandomResponseProvider
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.InteractiveComponentResponse
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import com.kreait.slack.api.contract.jackson.group.respond.RespondMessageRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SlackMessageSender @Autowired constructor(private val slackClient: SlackClient,
                                                private val randomResponseProvider: RandomResponseProvider,
                                                private val userChannelIdService: UserChannelIdService) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SlackMessageSender::class.java)
    }

    /**
     * determines if the channel is a direct message channel and returns the bot-user-channel for this [userId]
     */
    private fun resolveChannelId(messageContext: MessageContext,
                                 token: String): String? {
        return if (messageContext.isDirectMessage()) {
            userChannelIdService.fetchChannelIdByUserId(userId = messageContext.userId, accessToken = token)
        } else if (messageContext.channelId == null) {
            userChannelIdService.fetchChannelIdByUserId(userId = messageContext.userId, accessToken = token)
        } else {
            messageContext.channelId
        }
    }

    /**
     * sends an ephemeral message to the user, if the channel is a directmessage channel, the message will be sent directly to the user
     */
    fun sendEphemeralMessage(responseType: ResponseType,
                             attachments: List<Attachment> = listOf(),
                             messageContext: MessageContext,
                             token: String) {
        this.slackClient.respond().message(messageContext.responseUrl).with(RespondMessageRequest(
                text = randomResponseProvider.getRandomizedResponse(type = responseType, messageContext = messageContext),
                attachments = attachments,
                responseType = com.kreait.slack.api.contract.jackson.group.respond.ResponseType.EPHEMERAL))
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug("Successfully sent ${responseType.id} ephemeral message")
                    }
                }.onFailure {
                    LOG.error("failed to send ${responseType.id}: $it")
                }.invoke()

    }

    /**
     * sends an ephemeral message to the user, if the channel is a directmessage channel, the message will be sent directly to the user
     */
    fun sendCustomEphemeralMessage(attachments: List<Attachment> = listOf(),
                                   messageContext: MessageContext,
                                   token: String,
                                   message: String) {
        this.slackClient.respond().message(messageContext.responseUrl).with(RespondMessageRequest(
                text = message,
                attachments = attachments,
                responseType = com.kreait.slack.api.contract.jackson.group.respond.ResponseType.EPHEMERAL))
                .onFailure { LOG.error("failure while sending custom response: $it") }
                .invoke()
    }


    /**
     * sends an message to the user, if the channel is a directmessage channel, the message will be sent directly to the user
     */
    fun sendMessage(responseType: ResponseType,
                    attachments: List<Attachment> = listOf(),
                    messageContext: MessageContext,
                    token: String) {

        val id = resolveChannelId(messageContext, token)!!
        this.slackClient.chat().postMessage(token)
                .with(PostMessageRequest(
                        text = randomResponseProvider.getRandomizedResponse(type = responseType, messageContext = messageContext),
                        channel = id,
                        attachments = attachments
                ))
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug("Successfully sent ${responseType.id} message")
                    }
                }.onFailure {
                    LOG.error("failed to send ${responseType.id}: $it")
                }.invoke()
    }
}

/**
 * dataclass that represents the context of a message
 */
data class MessageContext(val currentStandup: String? = null,
                          val nextStandup: String? = null,
                          val date: LocalDate? = null,
                          val channelName: String? = null,
                          val channelId: String? = null,
                          val teamId: String,
                          val userId: String,
                          val responseUrl: String = "") {
    companion object {
        fun of(slackCommand: SlackCommand): MessageContext {
            return MessageContext(channelName = slackCommand.channelName, channelId = slackCommand.channelId, teamId = slackCommand.teamId, userId = slackCommand.userId, responseUrl = slackCommand.responseUrl)
        }

        fun of(component: InteractiveMessage): MessageContext {
            return MessageContext(channelName = component.channel.name, channelId = component.channel.id, teamId = component.team.id, userId = component.user.id, responseUrl = component.responseUrl!!)
        }
    }

    /**
     *  returns [true] if the channel is a direct-message channel
     */
    fun isDirectMessage(): Boolean {
        return this.channelName == "directmessage"
    }
}
