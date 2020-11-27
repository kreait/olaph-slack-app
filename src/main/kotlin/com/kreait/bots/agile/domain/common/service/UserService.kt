package com.kreait.bots.agile.domain.common.service


import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.ChannelType
import com.kreait.slack.api.contract.jackson.common.types.Member
import com.kreait.slack.api.contract.jackson.group.users.Channel
import com.kreait.slack.api.contract.jackson.group.users.ConversationsRequest
import com.kreait.slack.api.contract.jackson.group.users.InfoRequest
import com.kreait.slack.api.contract.jackson.group.users.ListRequest
import org.apache.commons.logging.LogFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service


@Service
class UserService constructor(private val slackClient: SlackClient) {

    companion object {
        private val LOG = LogFactory.getLog(UserService::class.java)
    }

    /**
     * Returns [User] information with access token from given team (includes Locale)
     */
    @Cacheable("UserService.user()")
    fun user(userId: String, token: String): Member {
        val result = this.slackClient.users()
                .info(token)
                .with(InfoRequest(userId, includeLocale = true))
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug(it)
                    }
                }
                .onFailure {
                    LOG.error(it)
                }
                .invoke()
        return result.success?.user
                ?: throw IllegalStateException("Couldnt obtain userinfo $userId ")
    }


    /**
     * Returns a list of [Member]s for given team
     */
    @Cacheable("UserService.userList()")
    fun userList(token: String): List<Member> {
        val result = this.slackClient.users()
                .list(token)
                .with(ListRequest(limit = 100))
                .onSuccess {
                    when {
                        LOG.isDebugEnabled -> LOG.debug(it)
                    }
                }
                .onFailure {
                    LOG.error(it)
                }
                .invoke()
        return result.success?.members
                ?: throw IllegalStateException("Users list for team $token could not be obtained")
    }

    /**
     * Returns a list of [Channel]s that are accessible for given team and user
     */
    //@Cacheable("UserService.conversationList()")
    fun conversationList(accessToken: String, userId: String): List<Channel> {
        val result = this.slackClient.users()
                .conversations(accessToken)
                .with(ConversationsRequest(excludeArchived = true, limit = 100, types = setOf(ChannelType.PUBLIC, ChannelType.PRIVATE), userId = userId))
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
                ?: throw IllegalStateException("Channel list for user $userId could not be obtained")
    }
}
