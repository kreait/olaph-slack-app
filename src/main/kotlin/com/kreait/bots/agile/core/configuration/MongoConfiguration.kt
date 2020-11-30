package com.kreait.bots.agile.core.configuration

import com.kreait.bots.agile.core.configuration.converter.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

@Configuration
class MongoConfiguration(@Value("\${spring.data.mongodb.database}") private val databaseName: String) : AbstractMongoClientConfiguration() {
    override fun getDatabaseName(): String = databaseName

    override fun customConversions(): MongoCustomConversions {
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