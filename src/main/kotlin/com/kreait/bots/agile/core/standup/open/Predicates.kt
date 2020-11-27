package com.kreait.bots.agile.core.standup.open

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.Standup.Status
import java.time.Instant

/**
 * checks whether a [Status] can be set to open
 * return [true] if given [Standup] fulfills given criteria
 */
fun Standup.Predicates.Companion.canTransitionToOpen(standup: Standup): Boolean {
    return standup.status == Status.CREATED
            && Instant.now().isAfter(standup.timestamp)

}
