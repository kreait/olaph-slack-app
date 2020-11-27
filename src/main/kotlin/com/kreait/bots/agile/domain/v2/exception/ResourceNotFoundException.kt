package com.kreait.bots.agile.domain.v2.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Thrown if a resource could not be found. If thrown, the app responds with 404 Not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)