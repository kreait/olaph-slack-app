package com.kreait.bots.agile.core.exception

import com.fasterxml.jackson.databind.JsonMappingException
import com.kreait.slack.broker.store.team.TeamNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class CommonExceptionHandler : ResponseEntityExceptionHandler() {


    companion object {
        private val LOG = LoggerFactory.getLogger(CommonExceptionHandler::class.java)
    }

    /**
     * Handle [ResourceNotFoundException]s
     * This will also handle [JsonMappingException]s and rethrow the cause of the exception which will allow us to use our own exceptions
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, body: Any?, headers: HttpHeaders, status: HttpStatus, request: WebRequest):
            ResponseEntity<Any> {

        return super.handleExceptionInternal(ex, body, headers, HttpStatus.NOT_FOUND, request)
    }

    /**
     * Handle [TeamNotFoundException]s
     * TeamNotFoundException is causing issue at this time so we return 200 to tell slack everything is okay
     */
    @ExceptionHandler(TeamNotFoundException::class)
    fun handleTeamNotFoundException(ex: TeamNotFoundException):
            ResponseEntity<Any> {

        LOG.error("{}", ex.message)

        return ResponseEntity.ok().build()
    }
}
