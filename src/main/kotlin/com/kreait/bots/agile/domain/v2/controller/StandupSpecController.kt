package com.kreait.bots.agile.domain.v2.controller

import com.kreait.bots.agile.core.auth.Authorize
import com.kreait.bots.agile.domain.v2.data.StandupSpec
import com.kreait.bots.agile.domain.v2.dto.of
import com.kreait.bots.agile.domain.v2.exception.ResourceNotFoundException
import com.kreait.bots.agile.domain.v2.repository.StandupSpecRepository
import com.kreait.bots.olaph.dto.jackson.common.StandupSpecRequest
import com.kreait.bots.olaph.dto.jackson.standupspec.CreateStandupSpecResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.DeleteStandupResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.ErrorCreateStandupSpecResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.ErrorDeleteStandupResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.ErrorListStandupResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.ListStandupResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.SuccessfulDeleteStandupResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.SuccessfulListStandupResponse
import com.kreait.bots.olaph.dto.jackson.standupspec.SuccessfulStandupSpecResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.DayOfWeek

/**
 * StandupSpec REST Controller
 */
@RestController
@RequestMapping("/standup-specs")
class StandupSpecController @Autowired constructor(private val standupSpecRepository: StandupSpecRepository) {
    companion object {

        val Log = LoggerFactory.getLogger(this.javaClass)
    }

    /**
     * Creates a new StandupSpec
     * @param standupSpecRequestDto the request body
     * @return the saved entity
     */
    @PostMapping
    @Authorize //Todo test authorize
    @ResponseStatus(HttpStatus.CREATED)
    fun createStandupSpec(@RequestBody(required = true) standupSpecRequestDto: StandupSpecRequest): CreateStandupSpecResponse {
        val standupSpec = this.standupSpecRepository.insert(StandupSpec.of(standupSpecRequestDto))
        return SuccessfulStandupSpecResponse.of(standupSpec)
    }

    /**
     * Gets a standupSpec
     * @param id the id of the entity
     * @return the entity
     * @throws ResourceNotFoundException if no standupSpec was found under the given id
     */
    @GetMapping("/{id}")
    @Authorize
    fun getStandupSpec(@PathVariable("id") id: String): CreateStandupSpecResponse {
        return this.standupSpecRepository.findById(id)
                .orElse(null)?.let {
                    SuccessfulStandupSpecResponse.of(it)
                } ?: ErrorCreateStandupSpecResponse(false, "StandupSpec $id not found")
    }

    @DeleteMapping("/{id}")
    @Authorize
    fun deleteStandup(@PathVariable("id") id: String): DeleteStandupResponse {
        return this.standupSpecRepository.findById(id)
                .orElse(null)?.let {
                    this.standupSpecRepository.save(it.copy(status = StandupSpec.Status.ARCHIVED))
                    SuccessfulDeleteStandupResponse(true)
                } ?: ErrorDeleteStandupResponse(false, "StandupSpec $id does not exist")
    }


    /**
     * Gets all (active) standupSpecs
     * @param withParticipantUserId userID that must be present in participants
     * @param onDayOfWeek day of week this stand-up is scheduled on
     * @param withOlaphAccountId olaph account this standupSpec belongs to
     * @return the entity
     * @throws ResourceNotFoundException if no standupSpec was found under the given id
     */
    @GetMapping
    @Authorize
    fun getAllStandupSpecs(@RequestParam("olaphAccountId", required = true) olaphAccountId: String,
                           @RequestParam("integrationUserId", required = false) integrationUserId: String? = null,
                           @RequestParam("onDayOfWeek", required = false) onDayOfWeek: String? = null)
            : ListStandupResponse {
        val standupspecs = this.standupSpecRepository
                .find(withParticipantUserId = integrationUserId,
                        onDayOfWeek = onDayOfWeek?.let {
                            DayOfWeek.valueOf(it)
                        },
                        withOlaphAccountId = olaphAccountId,
                        withStatus = StandupSpec.Status.ACTIVE)

        return if (standupspecs.isNullOrEmpty())
            ErrorListStandupResponse(false, "No Standup Specs found")
        else SuccessfulListStandupResponse.of(standupspecs)
    }
}

