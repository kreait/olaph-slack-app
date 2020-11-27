package com.kreait.bots.agile.core.standup.broadcast

import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.core.standup.open.StandupOpeningService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.standup.BroadcastingService
import com.kreait.bots.agile.domain.slack.standup.SlackBroadcastSender
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class BroadcastingServiceUnitTest {

    @DisplayName("Test BroadcastingService")
    @Test
    fun testBroadcastingService() {
        val standupRepository = mock<StandupRepository> {
            on { findAnsweredAndOpen(any()) } doReturn listOf(
                    Standup.sample().copy(status = Standup.Status.OPEN,
                            id = "sampleId",
                            answers = listOf(Standup.Answer("answer", "", 0)),
                            questions = listOf("question")))
        }
        val openingService = mock<StandupOpeningService>()
        val broadcastSender = mock<SlackBroadcastSender>()
        val broadcastingService = BroadcastingService(standupRepository, openingService, broadcastSender)
        broadcastingService.broadcast("sampleUser")
        verify(openingService, times(1)).openStandup(any(), any())
    }
}