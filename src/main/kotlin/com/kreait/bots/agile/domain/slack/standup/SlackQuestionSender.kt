package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.domain.common.service.UserChannelIdService
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import com.kreait.slack.broker.store.team.TeamStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SlackQuestionSender @Autowired constructor(private val slackClient: SlackClient,
                                                 private val userChannelIdService: UserChannelIdService,
                                                 private val teamStore: TeamStore) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SlackQuestionSender::class.java)
    }

    fun sendQuestion(userId: String, question: String, teamId: String): Boolean {
        val token = teamStore.findById(teamId).bot.accessToken
        val channel: String? = userChannelIdService.fetchChannelIdByUserId(userId, token)
        return channel?.let {
            this.slackClient.chat().postMessage(token)
                    .with(PostMessageRequest(text = question, channel = channel))
                    .onSuccess {
                        if (LOG.isDebugEnabled)
                            LOG.debug("Successfully sent question")
                    }
                    .onFailure {
                        LOG.error("Failed to send question")
                    }
                    .invoke().success != null
        } ?: false
    }

}
