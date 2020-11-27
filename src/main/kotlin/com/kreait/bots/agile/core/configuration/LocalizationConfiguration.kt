package com.kreait.bots.agile.core.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.*

@Configuration
class LocalizationConfiguration : WebMvcConfigurer {

    @Bean
    fun messageSourceAccessor(): MessageSourceAccessor {
        val messageSource = ResourceBundleMessageSource()
        messageSource.setBasename("values/messages")
        messageSource.setDefaultEncoding("UTF-8")
        messageSource.setFallbackToSystemLocale(false)
        return MessageSourceAccessor(messageSource, Locale.ENGLISH)
    }
}
