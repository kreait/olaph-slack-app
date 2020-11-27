package com.kreait.bots.agile.domain.slack.standupDefinition.leave

import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.service.UserChannelIdService
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.group.channels.ChannelsInfoRequest
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class MemberLeftMessageSender @Autowired constructor(private val userChannelIdService: UserChannelIdService,
                                                     private val message: MessageSourceAccessor,
                                                     private val slackClient: SlackClient) {
    companion object {
        const val CHANNEL_LEFT_OPEN = "channelLeftWithOpenStandups"
        const val CHANNEL_LEFT = "channelLeftMessage"
        private val Log = LoggerFactory.getLogger(MemberLeftMessageSender::class.java)
    }

    /**
     * If the user has an open standup we send a message that notifies that the standup has been closed
     */
    fun sendCancelOpenStandupMessage(userId: String, standup: StandupDefinition, accessToken: String) {
        // TODO this should use the SlackMessageSender
        userChannelIdService.fetchChannelIdByUserId(userId, accessToken)?.let {
            slackClient.chat()
                    .postMessage(accessToken)
                    .with(PostMessageRequest(
                            channel = it,
                            text = String.format(message.getMessage(CHANNEL_LEFT_OPEN, Locale.ENGLISH), standup.name)
                    ))
                    .onSuccess {
                        when {
                            Log.isDebugEnabled -> Log.debug("successfully sent left-broadcast-message {}", it)
                        }
                    }
                    .onFailure {
                        Log.error("failure during message-sending")
                    }
                    .invoke()
        }
    }

    /**
     * Send Confirmation Message that you left given [standups]
     */
    fun sendChannelLeftMessage(userId: String, standups: List<StandupDefinition>, channelId: String, accessToken: String) {
        userChannelIdService.fetchChannelIdByUserId(userId, accessToken)?.let {
            this.slackClient.channel().info(accessToken).with(ChannelsInfoRequest(channelId))
                    .onSuccess { channel ->
                        if (standups.isNotEmpty()) {
                            slackClient.chat()
                                    .postMessage(accessToken)
                                    .with(PostMessageRequest(
                                            channel = it,
                                            text = String.format(message.getMessage(CHANNEL_LEFT, Locale.ENGLISH), channel.channel.name),
                                            attachments = standups.map { Attachment(text = it.name, fallback = it.name) }
                                    ))
                                    .onSuccess {
                                        when {
                                            Log.isDebugEnabled -> Log.debug("successfully sent left-broadcast-message {}", it)
                                        }
                                    }
                                    .onFailure {
                                        Log.error("failure during message-sending")
                                    }
                                    .invoke()
                        }
                    }.onFailure {
                        Log.error("failed to retrieve channel info {}", it)
                    }.invoke()
        }
    }
}
