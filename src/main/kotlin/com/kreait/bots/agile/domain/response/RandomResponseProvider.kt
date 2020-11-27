package com.kreait.bots.agile.domain.response

import com.kreait.bots.agile.domain.common.data.RandomResponses
import com.kreait.bots.agile.domain.common.data.RandomResponsesRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.Random

/**
 * Provider that gives a random response
 */
@Service
class RandomResponseProvider @Autowired constructor(private val randomResponsesRepository: RandomResponsesRepository) {


    companion object {
        const val DATE_PLACEHOLDER = "{date}"
        const val CURRENT_STANDUP_NAME_PLACEHOLDER = "{current_standup}"
        const val NEXT_STANDUP_NAME_PLACEHOLDER = "{next_standup}"
    }

    fun getRandomizedResponse(type: ResponseType,
                              messageContext: MessageContext? = null): String {

        return randomResponsesRepository.findById(type.id)
                .map {
                    if (it.messages.isNotEmpty())
                        it.messages[Random().nextInt(it.messages.size)]
                    else
                        type.fallback
                }

                .map { message ->
                    messageContext?.date?.let { message.replace(DATE_PLACEHOLDER, it.toString()) } ?: message
                }
                .map { message ->
                    messageContext?.currentStandup?.let { message.replace(CURRENT_STANDUP_NAME_PLACEHOLDER, it) }
                            ?: message
                }
                .map { message ->
                    messageContext?.nextStandup?.let { message.replace(NEXT_STANDUP_NAME_PLACEHOLDER, it) } ?: message
                }

                .orElseThrow { ResponseMessageNotFound() }
    }

    fun getResponses(id: String): Optional<RandomResponses> {
        return randomResponsesRepository.findById(id)
    }

    fun addNewRandomizedResponse(id: String, messages: List<String>) {
        randomResponsesRepository.update(id, Update.update(RandomResponses.RESPONSES, messages))
    }
}

class ResponseMessageNotFound : RuntimeException()
