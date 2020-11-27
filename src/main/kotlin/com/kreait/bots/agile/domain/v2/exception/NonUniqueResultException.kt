package com.kreait.bots.agile.domain.v2.exception

import java.lang.RuntimeException

/**
 * Thrown if a query was expected to return a single value but did not return a unique result or no result at all.
 */
class NonUniqueResultException(message: String = "Query did not return a unique or no result") : RuntimeException(message)