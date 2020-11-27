package com.kreait.bots.agile.core.configuration

import com.kreait.bots.agile.core.configuration.converter.InstantToStringConverter
import com.kreait.bots.agile.core.configuration.converter.LocalDateToStringConverter
import com.kreait.bots.agile.core.configuration.converter.LocalTimeToStringConverter
import com.kreait.bots.agile.core.configuration.converter.StringToInstantConverter
import com.kreait.bots.agile.core.configuration.converter.StringToLocalDateConverter
import com.kreait.bots.agile.core.configuration.converter.StringToLocalTimeConverter
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.convert.CustomConversions
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.WriteResultChecking
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.lang.Nullable


/**
 * MongoDBConfiguration class that configures the spring data mongodb access and converters via [AbstractMongoConfiguration.mappingMongoConverter]
 */
@Configuration
@EnableMongoRepositories(basePackages = ["com.kreait.bots.agile"])
class MongoConfiguration @Autowired constructor(@Value("\${mongodb.databaseName}") private val databaseName: String,
                                                @Value("\${mongodb.commaSeparatedHosts}") private val commaSeparatedHosts: String,
                                                @Nullable @Value("\${mongodb.username:#{null}}") private val username: String?,
                                                @Nullable @Value("\${mongodb.authDatabaseName:#{null}}") private val authDatabaseName: String?,
                                                @Nullable @Value("\${mongodb.password:#{null}}") private val password: String?,
                                                @Nullable @Value("\${mongodb.sslEnabled:false}") private val sslEnabled: Boolean)
    : AbstractMongoConfiguration() {

    /**
     * Creates a [MongoClient] with one or more [ServerAddress]es
     * If a username and password is provided via -Dmongodb.username and -Dmongodb.password a credential object will be used to access the mongodb/cluster
     * @return [MongoClient] that holds connection data
     */
    override fun mongoClient(): MongoClient {
        val hosts = this.commaSeparatedHosts.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val serverAddresses = hosts.map { ServerAddress(it) }

        return if (username != null || password != null) {
            val credential = MongoCredential.createCredential(username!!, authDatabaseName!!, password!!.toCharArray())
            val builder = MongoClientOptions.builder()
            if (sslEnabled) {
                builder.sslEnabled(true)
                        .retryWrites(true)
            }
            MongoClient(serverAddresses, credential, builder.build())
        } else {
            MongoClient(serverAddresses, MongoClientOptions.builder().build())
        }
    }

    /**
     * Database name that should be used (gets created automatically if not existent)
     * @return database name
     */
    override fun getDatabaseName(): String {
        return this.databaseName
    }

    /**
     * [MongoTemplate] that will be used by spring data to execute mongodb operations
     * @return configured [MongoTemplate]
     */
    @Bean
    @Throws(Exception::class)
    override fun mongoTemplate(): MongoTemplate {
        val mongoTemplate = MongoTemplate(mongoDbFactory(), mappingMongoConverter())
        mongoTemplate.setWriteResultChecking(WriteResultChecking.NONE)
        return mongoTemplate
    }

    override fun customConversions(): CustomConversions {

        return MongoCustomConversions(
                listOf(
                        LocalTimeToStringConverter.INSTANCE,
                        StringToLocalTimeConverter.INSTANCE,
                        LocalDateToStringConverter.INSTANCE,
                        StringToLocalDateConverter.INSTANCE,
                        InstantToStringConverter.INSTANCE,
                        StringToInstantConverter.INSTANCE
                )
        )
    }


}
