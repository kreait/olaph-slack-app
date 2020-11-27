package com.kreait.bots.agile.domain.slack.standup.statistics

import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.ArrayList


@Controller
class StatistikController @Autowired constructor(private val standupRepository: StandupRepository) {


    fun getDauforMonths(startDate: LocalDate, endDate: LocalDate): Int {
        val dates = getDatesOfRange(startDate, endDate)
        val activeUsers = standupRepository.getDailyActiveUsers(dates)
        println(activeUsers)
        val sum = activeUsers.sumBy { it.amount.toInt() }
        return sum

    }

    private fun getDatesOfRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
        val dates = ArrayList<LocalDate>()
        var tmp = startDate
        while (tmp.isBefore(endDate) || tmp == endDate) {
            if (tmp.dayOfWeek != DayOfWeek.SUNDAY && tmp.dayOfWeek != DayOfWeek.SATURDAY)
                dates.add(tmp)
            tmp = tmp.plusDays(1)
        }
        return dates
    }

}