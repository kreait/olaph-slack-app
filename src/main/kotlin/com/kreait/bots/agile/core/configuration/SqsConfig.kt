package com.kreait.bots.agile.core.configuration

import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver


/**
 * creates & registers the needed jms beans
 * @param endpoint the endpoint of the sqs
 * @param queueName the name of the sqs
 * @param sqsListener the messagelistener which is used to receive messages from the sqs
 * @param awsAccessKey the aws access key
 * @param awsSecretKey the aws secret key
 */
@Configuration
class SqsConfig {

    /**
     * registers a bean for the QueueMessageHandlerFactory
     */
    @Bean
    fun queueMessageHandlerFactory(): QueueMessageHandlerFactory {
        val factory = QueueMessageHandlerFactory()
        val messageConverter = MappingJackson2MessageConverter()
        messageConverter.isStrictContentTypeMatch = false
        factory.setArgumentResolvers(mutableListOf<HandlerMethodArgumentResolver>(PayloadArgumentResolver(messageConverter)))
        return factory
    }
}
