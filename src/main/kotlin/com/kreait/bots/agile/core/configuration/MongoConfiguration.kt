package com.kreait.bots.agile.core.configuration

import com.kreait.bots.agile.core.configuration.converter.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
class MongoConfiguration {

    @Bean
    fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(
            listOf(
                LocalTimeToStringConverter.INSTANCE,
                StringToLocalTimeConverter.INSTANCE,
                LocalDateToStringConverter.INSTANCE,
                StringToLocalDateConverter.INSTANCE,
                InstantToStringConverter.INSTANCE,
                StringToInstantConverter.INSTANCE,
            )
        )

    }
}