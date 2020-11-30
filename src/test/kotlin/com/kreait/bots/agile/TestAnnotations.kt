package com.kreait.bots.agile

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test


@Test
@Tag("Integration")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class IntegrationTest

@Test
@Tag("Unit")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class UnitTest
