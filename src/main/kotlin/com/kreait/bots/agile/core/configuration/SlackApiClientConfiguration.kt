package com.kreait.bots.agile.core.configuration

import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.spring.SpringSlackClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration class that configures the [SpringSlackClient]
 * @constructor takes a slack app authentication token
 */
@Configuration
class SlackApiClientConfiguration {

    @Bean
    fun slackApiClient(): SlackClient {
        return SpringSlackClient()
    }
}
