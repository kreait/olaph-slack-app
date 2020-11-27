package com.kreait.bots.agile.core.configuration

import io.micrometer.core.instrument.config.MeterFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudWatchMeterConfig {

    @Bean
    fun olaphMeterFilter(): MeterFilter {
        return MeterFilter.denyUnless { meter ->
            meter.name.startsWith("slack") || meter.name.startsWith("olaph")
        }
    }
}
