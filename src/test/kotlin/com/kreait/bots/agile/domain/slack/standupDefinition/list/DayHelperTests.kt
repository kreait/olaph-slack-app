package com.kreait.bots.agile.domain.slack.standupDefinition.list

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


@DisplayName("Dayhelper Tests")
class DayHelperTests {


    @Test
    @DisplayName("Test Weekly")
    fun testWeekly() {

        val standupTime = LocalTime.of(14, 0)
        val standupWeekDays = listOf(DayOfWeek.TUESDAY)

        val sameDayAfterTime = DayHelper.getNextStandupDay(
                standupWeekDays,
                standupTime,
                0,
                LocalDateTime.of(LocalDate.of(2019, 2, 26), standupTime.plusMinutes(1))
        )

        Assertions.assertEquals("next Tuesday", sameDayAfterTime)

        val sameDayBeforeTime = DayHelper.getNextStandupDay(
                standupWeekDays,
                standupTime,
                0,
                LocalDateTime.of(LocalDate.of(2019, 2, 26), standupTime.minusHours(1))
        )

        Assertions.assertEquals("today", sameDayBeforeTime)
    }

    @Test
    @DisplayName("Test next day")
    fun testNextDay() {

        val standupTime = LocalTime.of(14, 0)
        val standupWeekDays = listOf(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)

        val tomorrow = DayHelper.getNextStandupDay(
                standupWeekDays,
                standupTime,
                0,
                LocalDateTime.of(LocalDate.of(2019, 2, 26), standupTime.plusMinutes(1))
        )

        Assertions.assertEquals("tomorrow", tomorrow)
    }


    @Test
    @DisplayName("Test day after tomorrow")
    fun testDayAfterTomorrow() {

        val standupTime = LocalTime.of(14, 0)
        val standupWeekDays = listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)

        val dayAfterTomorrow = DayHelper.getNextStandupDay(
                standupWeekDays,
                standupTime,
                0,
                LocalDateTime.of(LocalDate.of(2019, 2, 26), standupTime.plusMinutes(1))
        )

        Assertions.assertEquals("Thursday", dayAfterTomorrow)
    }
}
