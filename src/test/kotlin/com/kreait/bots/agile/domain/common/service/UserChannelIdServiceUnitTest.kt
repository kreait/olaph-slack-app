package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.api.contract.jackson.group.im.ErrorImOpenResponse
import com.kreait.slack.api.contract.jackson.group.im.SuccessfulImOpenResponse
import com.kreait.slack.api.contract.jackson.group.im.sample
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class UserChannelIdServiceUnitTest {

    @DisplayName("Test Successful Fetch Userid")
    @Test
    fun testFetchUserId() {
        val slackClient = MockSlackClient()
        val expected = SuccessfulImOpenResponse.sample().copy(channel = SuccessfulImOpenResponse.Channel.sample().copy(id = "sampleChannel"))
        slackClient.im().open("").successResponse = expected
        val userChannelIdService = UserChannelIdService(slackClient, mock())
        val response = userChannelIdService.fetchChannelIdByUserId("sampleUser", "sampleTeam")
        Assertions.assertEquals(expected.channel.id, response)
    }

    @DisplayName("Test Failure Fetch Userid")
    @Test
    fun testFetchUserIdFailure() {
        val slackClient = MockSlackClient()
        val expected = ErrorImOpenResponse.sample().copy(error = "account_inactive")
        slackClient.im().open("").failureResponse = expected
        val mockRepo = mock<StandupRepository>()
        val userChannelIdService = UserChannelIdService(slackClient, mockRepo)
        userChannelIdService.fetchChannelIdByUserId("sampleUser", "sampleTeam")

        verify(mockRepo, times(1)).update(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
    }
}