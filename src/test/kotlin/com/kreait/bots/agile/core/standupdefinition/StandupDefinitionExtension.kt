package com.kreait.bots.agile.core.standupdefinition

import com.kreait.bots.agile.domain.common.data.StandupDefinition
import java.time.DayOfWeek
import java.time.LocalTime

fun StandupDefinition.Companion.sample(): StandupDefinition {
    return StandupDefinition(name = "Workspace", days = listOf(DayOfWeek.WEDNESDAY), time = LocalTime.NOON,
            broadcastChannelId = "channel", questions = listOf("what"), teamId = "TEAM1", subscribedUserIds = listOf("USER1"))
}
