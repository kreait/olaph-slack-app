package com.kreait.bots.agile.core.standup.question

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.Standup.Status

/**
 * checks whether a question can be send
 * return [true] if given [Standup] fulfills given criteria
 */
fun Standup.Predicates.Companion.canSendQuestion(standup: Standup) =
            standup.questionsAsked == standup.answers.size
                    && standup.status == Status.OPEN
                    && standup.questions.isNotEmpty()
                    && standup.questionsAsked < standup.questions.size
