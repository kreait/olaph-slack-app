package com.kreait.bots.agile.domain.v2.configuration


import com.amazonaws.services.sns.AmazonSNS
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration of [NotificationMessagingTemplate]
 */
@Configuration
class NotificationConfiguration {

    @Bean
    fun notificationMessagingTemplate(amazonSNS: AmazonSNS): NotificationMessagingTemplate {
        return NotificationMessagingTemplate(amazonSNS)
    }
}
