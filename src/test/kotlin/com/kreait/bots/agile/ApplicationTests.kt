package com.kreait.bots.agile

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(classes = [(TestApplication::class)])
@TestPropertySource(properties = ["slack.token=test-token"])
@IntegrationTest
class ApplicationTests {

    /**
     * This is an empty test that just checks that the application context can be loaded.
     */
    @DisplayName("Application Context Loads")
    @Test
    fun contextLoads() {
    }

}
