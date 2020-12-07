package com.kreait.bots.agile.domain.common.service

import com.kreait.bots.agile.UnitTest
import com.kreait.slack.api.contract.jackson.ChannelType
import com.kreait.slack.api.contract.jackson.common.types.Conversation
import com.kreait.slack.api.contract.jackson.common.types.Member
import com.kreait.slack.api.contract.jackson.common.types.sample
import com.kreait.slack.api.contract.jackson.group.conversations.ConversationsListRequest
import com.kreait.slack.api.contract.jackson.group.conversations.SuccessfulConversationListResponse
import com.kreait.slack.api.contract.jackson.group.conversations.SuccessfulConversationMembersResponse
import com.kreait.slack.api.contract.jackson.group.conversations.sample
import com.kreait.slack.api.test.MockSlackClient
import com.kreait.slack.broker.store.team.Team
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@UnitTest
class ConversationServiceTest {

    @DisplayName("Test Conversation Service")
    @Test
    fun testConversationList() {
        val slackClient = MockSlackClient()
        slackClient.conversation().list("").successResponse = SuccessfulConversationListResponse.sample()
                .copy(channels = listOf(Conversation.sample()))
        val service = ConversationService(slackClient, mock())
        val expected = ConversationsListRequest(excludeArchived = true, limit = 200, types = setOf(ChannelType.PUBLIC, ChannelType.PRIVATE))
        val response = service.conversationList("sampleTeam",
                Team("", "",
                        Team.Bot("", "")))
        Assertions.assertEquals(response[0].id, "")
        Assertions.assertEquals(expected, slackClient.conversation().list("").params())
    }

    @DisplayName("Test getNonBotMembers method")
    @Test
    fun testGetNonBotMembers() {
        val slackClient = MockSlackClient()
        slackClient.conversation().members("").successResponse =
                SuccessfulConversationMembersResponse.sample().copy(true, listOf("user", "bot"))
        val userService = mock<UserService> {
            on {
                userList(any())
            } doReturn listOf(Member.sample().copy(id = "bot", isBot = true),
                    Member.sample().copy(id = "user", isBot = false))
        }
        val service = ConversationService(slackClient, userService)
        val response = service.getNonBotMembersFromChannel("sample_channel", "")
        Assertions.assertEquals(response.size, 1)
        Assertions.assertEquals(response[0], "user")
    }
}
