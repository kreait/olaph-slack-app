package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.UserChannelIdService
import com.kreait.bots.agile.domain.response.RandomResponseProvider
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import com.kreait.slack.broker.store.team.TeamStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SlackOpeningMessageSender @Autowired constructor(private val slackClient: SlackClient,
                                                       private val userChannelIdService: UserChannelIdService,
                                                       private val randomResponseProvider: RandomResponseProvider,
                                                       private val teamStore: TeamStore) {


    companion object {
        private val LOG = LoggerFactory.getLogger(SlackOpeningMessageSender::class.java)
    }

    fun sendOpeningMessage(standup: Standup): Successful {
        val token = teamStore.findById(standup.teamId).bot.accessToken
        val channel: String? = userChannelIdService.fetchChannelIdByUserId(standup.userId, token)
        channel?.let {

            val response = this.slackClient.chat()
                    .postMessage(token)
                    .with(PostMessageRequest(
                            channel = channel,
                            text = randomResponseProvider.getRandomizedResponse(ResponseType.OPENING,
                                    MessageContext(date = standup.date, currentStandup = standup.name, teamId = standup.teamId, userId = standup.userId))))
                    .onSuccess {
                        when {
                            LOG.isDebugEnabled -> LOG.debug("Successfully sent opening message")
                        }
                    }.onFailure {
                        LOG.error("Failed to send opening message")
                    }
                    .invoke()

            //TODO this if statement is hard to understand, maybe we should deal with exceptions and exceptionhandler here
            return if (response.success != null)
                Successful(success = true)
            else Successful(false, response.failure!!.error)
        } ?: return Successful(false, "channel not found")
    }
}

data class Successful(val success: Boolean, val errorCode: String = "")
