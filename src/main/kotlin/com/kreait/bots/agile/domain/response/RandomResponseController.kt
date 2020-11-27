package com.kreait.bots.agile.domain.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.kreait.bots.agile.core.auth.Authorize
import com.kreait.bots.agile.core.exception.ResourceNotFoundException
import com.kreait.bots.agile.domain.common.data.RandomResponses
import com.kreait.bots.agile.domain.common.data.RandomResponsesRepository
import com.kreait.slack.api.contract.jackson.util.JacksonDataClass
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
@RequestMapping("/random-answers")
class RandomAnswerController @Autowired constructor(private val randomResponsesRepository: RandomResponsesRepository,
                                                    private val randomResponseProvider: RandomResponseProvider) {

    @GetMapping()
    fun getAllRandomAnswers(): List<RandomResponses> = randomResponsesRepository.findAll()

    @GetMapping("/{randomAnswersId}")
    fun getRandomAnswer(@PathVariable("randomAnswersId") randomAnswersId: String): RandomResponses {
        return this.randomResponseProvider.getResponses(randomAnswersId).orElseThrow { ResourceNotFoundException() }
    }

    @Authorize
    @PutMapping("/{randomAnswersId}")
    fun updateRandomAnswer(@PathVariable("randomAnswersId") randomAnswersId: String,
                           @RequestBody requestBody: AddRandomResponsesRequest) {
        this.randomResponseProvider.addNewRandomizedResponse(randomAnswersId, requestBody.messages)
    }
}

@JacksonDataClass
data class AddRandomResponsesRequest(@JsonProperty("messages") val messages: List<String>)
