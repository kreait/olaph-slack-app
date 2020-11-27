package com.kreait.bots.agile.core.standup.data.repository

import com.kreait.bots.agile.domain.common.data.Standup

fun Standup.Companion.sample(): Standup {
    return Standup(
            broadcastChannelId = "",
            name = "",
            questions = listOf("Question1", "question2"),
            standupDefinitionId = "",
            userId = ""
    )
}
