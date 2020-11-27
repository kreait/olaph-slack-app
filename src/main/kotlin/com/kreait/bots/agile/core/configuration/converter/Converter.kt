package com.kreait.bots.agile.core.configuration.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField


@WritingConverter
enum class LocalTimeToStringConverter : Converter<LocalTime, String> {

    INSTANCE;

    override fun convert(source: LocalTime): String {
        return source.format(
                DateTimeFormatterBuilder()
                        .appendValue(ChronoField.HOUR_OF_DAY, 2)
                        .appendLiteral(':')
                        .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                        .toFormatter())
    }
}

@ReadingConverter
enum class StringToLocalTimeConverter : Converter<String, LocalTime> {

    INSTANCE;

    override fun convert(source: String): LocalTime {
        return LocalTime.parse(source)

    }
}

@WritingConverter
enum class LocalDateToStringConverter : Converter<LocalDate, String> {

    INSTANCE;

    override fun convert(source: LocalDate): String {
        return source.format(
                DateTimeFormatterBuilder()
                        .appendValue(ChronoField.YEAR, 4)
                        .appendLiteral('-')
                        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                        .appendLiteral('-')
                        .appendValue(ChronoField.DAY_OF_MONTH, 2)
                        .toFormatter())
    }
}

@ReadingConverter
enum class StringToLocalDateConverter : Converter<String, LocalDate> {

    INSTANCE;

    override fun convert(source: String): LocalDate {
        return LocalDate.parse(source)

    }
}


@WritingConverter
enum class InstantToStringConverter : Converter<Instant, String> {

    INSTANCE;

    override fun convert(source: Instant): String {
        return source.toString()
    }
}

@ReadingConverter
enum class StringToInstantConverter : Converter<String, Instant> {

    INSTANCE;

    override fun convert(source: String): Instant {
        return Instant.parse(source)

    }
}

