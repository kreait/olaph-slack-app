package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.slack.api.contract.jackson.group.conversations.ErrorConversationOpenResponse
import com.kreait.slack.api.contract.jackson.group.conversations.SuccessfulConversationOpenResponse
import com.kreait.slack.api.contract.jackson.group.conversations.sample
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class UserChannelIdServiceUnitTest {

    @DisplayName("Test Successful Fetch Userid")
    @Test
    fun testFetchUserId() {
        val slackClient = MockSlackClient()
        val expected = SuccessfulConversationOpenResponse.sample()
            .copy(channel = SuccessfulConversationOpenResponse.Channel.sample().copy(id = "sampleChannel"))
        slackClient.conversation().open("").successResponse = expected
        val userChannelIdService = UserChannelIdService(slackClient, mock())
        val response = userChannelIdService.fetchChannelIdByUserId("sampleUser", "sampleTeam")
        Assertions.assertEquals(expected.channel.id, response)
    }

    @DisplayName("Test Failure Fetch Userid")
    @Test
    fun testFetchUserIdFailure() {
        val slackClient = MockSlackClient()
        val expected = ErrorConversationOpenResponse.sample().copy(error = "account_inactive")
        slackClient.conversation().open("").failureResponse = expected
        val mockRepo = mock<StandupRepository>()
        val userChannelIdService = UserChannelIdService(slackClient, mockRepo)
        userChannelIdService.fetchChannelIdByUserId("sampleUser", "sampleTeam")

        verify(mockRepo, times(1)).update(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        )
    }
}
