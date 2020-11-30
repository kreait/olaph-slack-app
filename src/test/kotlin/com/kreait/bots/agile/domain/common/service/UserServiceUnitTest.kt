package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.UnitTest
import com.kreait.slack.api.contract.jackson.group.users.ErrorConversationsResponse
import com.kreait.slack.api.contract.jackson.group.users.ErrorInfoResponse
import com.kreait.slack.api.contract.jackson.group.users.ErrorListResponse
import com.kreait.slack.api.contract.jackson.group.users.SuccessfulConversationsResponse
import com.kreait.slack.api.contract.jackson.group.users.SuccessfulInfoResponse
import com.kreait.slack.api.contract.jackson.group.users.SuccessfulListResponse
import com.kreait.slack.api.contract.jackson.group.users.sample
import com.kreait.slack.api.test.MockSlackClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class UserServiceUnitTest {

    @DisplayName("Test Successful User")
    @Test
    fun testUserSuccess() {
        val slackClient = MockSlackClient()
        slackClient.users().info("").successResponse = SuccessfulInfoResponse.sample()
        val userService = UserService(slackClient)
        Assertions.assertNotNull(userService.user("sampleUser", "sampleTeam"))
    }

    @DisplayName("Test Fail User")
    @Test
    fun testUserFailure() {
        val slackClient = MockSlackClient()
        slackClient.users().info("").failureResponse = ErrorInfoResponse.sample()
        val userService = UserService(slackClient)
        Assertions.assertThrows(IllegalStateException::class.java) {
            userService.user("sampleUser", "sampleTeam")
        }
    }

    @DisplayName("Test Successful UserList")
    @Test
    fun testUserListSuccess() {
        val slackClient = MockSlackClient()
        val expectedResponse = SuccessfulListResponse.sample()

        slackClient.users().list("").successResponse = expectedResponse

        val userService = UserService(slackClient)
        Assertions.assertEquals(userService.userList("sampleTeam"), expectedResponse.members)
    }

    @DisplayName("Test Fail Userlist")
    @Test
    fun testUserListFailure() {
        val slackClient = MockSlackClient()
        slackClient.users().list("").failureResponse = ErrorListResponse.sample()
        val userService = UserService(slackClient)
        Assertions.assertThrows(IllegalStateException::class.java) {
            userService.userList("sampleTeam")
        }
    }

    @DisplayName("Test Successful ConversationList")
    @Test
    fun testConversationListSuccess() {
        val slackClient = MockSlackClient()
        val expectedResponse = SuccessfulConversationsResponse.sample()

        slackClient.users().conversations("").successResponse = expectedResponse

        val userService = UserService(slackClient)
        Assertions.assertEquals(userService.conversationList("sampleTeam", "sampleUser"), expectedResponse.channels)
    }

    @DisplayName("Test Fail ConversationList")
    @Test
    fun testConversationListFailure() {
        val slackClient = MockSlackClient()
        slackClient.users().conversations("").failureResponse = ErrorConversationsResponse.sample()
        val userService = UserService(slackClient)
        Assertions.assertThrows(IllegalStateException::class.java) {
            userService.conversationList("sampleTeam", "sampleUser")
        }
    }
}