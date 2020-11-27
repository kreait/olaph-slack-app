package com.kreait.bots.agile.domain.common.service

import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.ChannelType
import com.kreait.slack.api.contract.jackson.common.types.Conversation
import com.kreait.slack.api.contract.jackson.group.conversations.ConversationMembersRequest
import com.kreait.slack.api.contract.jackson.group.conversations.ConversationsListRequest
import com.kreait.slack.broker.store.team.Team
import org.apache.commons.logging.LogFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class ConversationService(private val slackClient: SlackClient, private val userService: UserService) {

    companion object {
        private val LOG = LogFactory.getLog(ConversationService::class.java)
    }

    @Cacheable("ConversationService.conversationList()")
    fun conversationList(teamId: String, team: Team): List<Conversation> {
        val result = this.slackClient.conversation()
                .list(team.bot.accessToken)
                .with(ConversationsListRequest(excludeArchived = true, limit = 200, types = setOf(ChannelType.PUBLIC, ChannelType.PRIVATE)))
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug(it)
                    }
                }
                .onFailure {
                    LOG.error(it)
                }
                .invoke()

        return result.success?.channels
                ?: throw IllegalStateException("Channels for team $teamId could not be obtained")
    }

    /**
     * Gets non bot members for given channel
     */
    fun getNonBotMembersFromChannel(channelId: String, token: String): List<String> {

        val botUserIds = userService.userList(token).filter { it.isBot }
                .map { it.id }

        return this.slackClient.conversation()
                .members(token)
                .with(ConversationMembersRequest(channelId))
                .onFailure {
                    LOG.error("Failure: $it")
                }
                .invoke().success
                ?.memberIds
                ?.filter { !botUserIds.contains(it) }
                ?: throw IllegalStateException("NonBot users for team $token could not be obtained")

    }

}
