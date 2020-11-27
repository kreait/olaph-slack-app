package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.group.im.ImOpenRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class UserChannelIdService @Autowired constructor(private val slackClient: SlackClient,
                                                  private val standupRepository: StandupRepository) {

    companion object {
        private val LOG = LoggerFactory.getLogger(UserChannelIdService::class.java)
    }

    /**
     * fetches the bot-user message channel or opens a new one
     */
    fun fetchChannelIdByUserId(userId: String, accessToken: String): String? {
        return slackClient.im()
                .open(accessToken)
                .with(ImOpenRequest(userId))
                .onFailure {
                    LOG.error("Error fetching channel by userId $it")
                    //TODO Find better solution after talking to slack support
                    if (it.error == "account_inactive" || it.error == "cannot_dm_bot") {
                        //TODO this should not be in this method, its too implicit
                        standupRepository.update(
                                withUserIds = setOf(userId),
                                withStatus = setOf(Standup.Status.CREATED),
                                update = Update.update(Standup.STATUS, Standup.Status.CANCELLED)
                        )
                    }
                }
                .invoke().success?.channel?.id
    }
}
