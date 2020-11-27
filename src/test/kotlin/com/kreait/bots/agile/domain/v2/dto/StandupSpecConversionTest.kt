package com.kreait.bots.agile.domain.v2.dto

import com.kreait.bots.agile.domain.v2.data.StandupSpec
import com.kreait.bots.agile.domain.v2.sample
import com.kreait.bots.olaph.dto.jackson.standupspec.SuccessfulStandupSpecResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StandupSpecConversionTest {

    @Test
    fun of() {
        val standupSpec = StandupSpec.sample()
        val standupSpecResponse = SuccessfulStandupSpecResponse.of(standupSpec).standupSpecResponse

        assertEquals(standupSpec.id, standupSpecResponse.id)
        assertEquals(standupSpec.name, standupSpecResponse.name)
        assertEquals(standupSpec.timezone, standupSpecResponse.timezone)
        assertEquals(standupSpec.broadcastChannelIds, standupSpecResponse.broadcastChannelIds)
        assertEquals(standupSpec.authorId, standupSpecResponse.authorId)
        assertEquals(standupSpec.accountId, standupSpecResponse.accountId)

        assertEquals(standupSpec.participants.size, standupSpecResponse.participants.size)
        assertEquals(standupSpec.questions.size, standupSpecResponse.questions.size)
        assertEquals(standupSpec.days.size, standupSpecResponse.days.size)

    }

}