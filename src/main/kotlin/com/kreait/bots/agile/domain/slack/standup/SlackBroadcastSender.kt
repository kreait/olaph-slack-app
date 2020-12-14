package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.domain.common.service.UserChannelIdService
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.bots.agile.domain.response.RandomResponseProvider
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.common.messaging.Attachment
import com.kreait.slack.api.contract.jackson.common.messaging.Color
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import com.kreait.slack.broker.store.team.TeamStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.Locale

@Service
class SlackBroadcastSender @Autowired constructor(private val slackClient: SlackClient,
                                                  private val userChannelIdService: UserChannelIdService,
                                                  private val userService: UserService,
                                                  private val message: MessageSourceAccessor,
                                                  private val randomResponseProvider: RandomResponseProvider,
                                                  private val teamStore: TeamStore) {

    companion object {
        private const val ATTACHMENT_ANSWERED_FALLBACK = "attachmentHasAnsweredFallback"
        private const val CONFIRMATION_TITLE = "confirmationTitle"
        private const val STANDUP_ANSWER_TITLE = "standupAnswerTitle"

        private val LOG = LoggerFactory.getLogger(SlackBroadcastSender::class.java)
    }

    fun sendBroadcast(userId: String, standupName: String, broadcastChannelId: String, date: LocalDate, questions: List<String>, answers: List<String>, teamId: String) {
        val token = teamStore.findById(teamId).bot.accessToken
        val userInfo = userService.user(userId, token)
        val realName = userInfo.realName
        val attachments = mutableListOf(

                Attachment(fallback = String.format(message.getMessage(ATTACHMENT_ANSWERED_FALLBACK, Locale.ENGLISH), realName, standupName),
                        color = Color.GOOD,
                        authorName = "",
                        title = standupName,
                        text = date.toString()))

        for ((index, answer) in answers.withIndex()) {
            attachments.add(Attachment(fallback = message.getMessage(STANDUP_ANSWER_TITLE, Locale.ENGLISH),
                    title = questions[index],
                    text = answer))
        }

        this.slackClient.chat()
                .postMessage(token)
                .with(PostMessageRequest(attachments = attachments, channel = broadcastChannelId, username = realName, iconUrl = userInfo.profile.image192, text = "",asUser = true))
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug("Successfully sent broadcast message")
                    }
                }
                .onFailure {
                    LOG.error("Error while sending broadcast message {}",it.error)
                }
                .invoke().success
    }


    //TODO why is this returning a boolean?
    fun sendBroadcastConfirmation(userId: String, teamId: String): Boolean {
        val token = teamStore.findById(teamId).bot.accessToken
        val channel: String? = userChannelIdService.fetchChannelIdByUserId(userId, token)
        return channel?.let {
            this.slackClient.chat()
                    .postMessage(token)
                    .with(PostMessageRequest(
                            text = randomResponseProvider.getRandomizedResponse(ResponseType.BROADCAST_CONFIRMATION),
                            attachments = listOf(
                                    Attachment("", message.getMessage(CONFIRMATION_TITLE, Locale.ENGLISH), "", "", Color.GOOD, "", "", listOf(),
                                            "", "")),
                            channel = channel))
                    .onSuccess {
                        when {
                            LOG.isDebugEnabled -> LOG.debug("Successfully sent broadcast confirmation")
                        }
                    }.onFailure {
                        LOG.error("Failed to send broadcast confirmation")
                    }
                    .invoke().success != null
        } ?: false
    }
}
