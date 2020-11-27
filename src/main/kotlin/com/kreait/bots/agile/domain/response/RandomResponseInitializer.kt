package com.kreait.bots.agile.domain.response

import com.kreait.bots.agile.domain.common.data.RandomResponses
import com.kreait.bots.agile.domain.common.data.RandomResponsesRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
class RandomResponseInitializer constructor(private val randomResponsesRepository: RandomResponsesRepository) : CommandLineRunner {


    override fun run(vararg args: String?) {
        for (value in ResponseType.values()) {
            val responses = randomResponsesRepository.findById(value.id)
            if (responses.isPresent) {
                randomResponsesRepository.update(value.id, Update.update(RandomResponses.DESCRIPTION, value.description))
            } else {
                randomResponsesRepository.save(RandomResponses(value.id, emptyList(), value.description))
            }
        }
    }

}
