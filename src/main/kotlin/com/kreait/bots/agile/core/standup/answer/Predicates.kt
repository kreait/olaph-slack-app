package com.kreait.bots.agile.core.standup.answer

import com.kreait.bots.agile.domain.common.data.Standup

/**
 * checks whether an [Standup.Answer] already exists
 * because slack sends sometimes duplicate messages
 * returns true if an answer can be saved
 */
fun Standup.Predicates.canSaveAnswer(userId: String, answer: Standup.Answer) =
        !this.standupRepository.exists(withUserIds = setOf(userId), hasAnswer = answer)
