package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.broker.store.team.TeamStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


/**
 * Service that handles messages which are not related to standups
 */
@Service
class RandomAnswerService @Autowired constructor(private val messageSender: SlackMessageSender,
                                                 private val teamStore: TeamStore) {
    //TODO talk about a more suitable name 
    fun handleMessage(userId: String, message: String, teamId: String) {
        val token = teamStore.findById(teamId).bot.accessToken
        when {
            message.contains("hoot hoot", ignoreCase = true) -> {
                messageSender.sendMessage(ResponseType.HOOT_RESPONSES, messageContext = MessageContext(userId = userId, teamId = teamId), token = token)
            }
            else -> {
                messageSender.sendMessage(ResponseType.UNKNOWN_MESSAGE_RESPONSE, messageContext = MessageContext(userId = userId, teamId = teamId), token = token)
            }
        }
    }
}
