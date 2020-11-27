package com.kreait.bots.agile.core.standup.close

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant


@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["slack.token=test-token"])
class StandupCancelServiceUnitTest @Autowired constructor(private val standupRepository: StandupRepository) {

    @BeforeEach
    fun setup() {
        standupRepository.deleteAll()
    }

    @DisplayName("Test standup cancel service")
    @Test
    fun testStandupCancelService() {
        standupRepository.insert(Standup.sample().copy(status = Standup.Status.OPEN, timestamp = Instant.now().minus(Duration.ofDays(2))))
        val messageSender = mock<SlackMessageSender>()
        val standupCancelService = StandupCancelService(standupRepository, messageSender, mock())
        standupCancelService.cancelOutdatedStandups()
    }
}