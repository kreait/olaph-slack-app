package com.kreait.bots.agile.domain.slack.standup.trigger

import com.kreait.bots.agile.IntegrationTest
import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.core.standup.data.repository.sample
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.group.chat.PostEphemeralRequest
import com.kreait.slack.api.contract.jackson.group.chat.SuccessfulPostEphemeralResponse
import com.kreait.slack.api.contract.jackson.group.chat.sample
import com.kreait.slack.api.contract.jackson.group.dialog.Dialog
import com.kreait.slack.api.contract.jackson.group.dialog.Options
import com.kreait.slack.api.contract.jackson.group.dialog.SelectElement
import com.kreait.slack.api.contract.jackson.group.dialog.SlackOpenDialogRequest
import com.kreait.slack.api.contract.jackson.group.dialog.SuccessfulOpenDialogResponse
import com.kreait.slack.api.contract.jackson.group.dialog.Type
import com.kreait.slack.api.contract.jackson.group.dialog.sample
import com.kreait.slack.api.contract.jackson.sample
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@IntegrationTest
@SpringBootTest(classes = [TestApplication::class])
class TriggerStandupDialogTest @Autowired constructor(private val standupRepository: StandupRepository) {

    private lateinit var slackClient: MockSlackClient

    private lateinit var command: SlackCommand

    private lateinit var triggerStandupDialog: TriggerStandupDialog

    @BeforeEach
    fun setup() {

        slackClient = MockSlackClient()
        standupRepository.deleteAll()
        command = SlackCommand.sample().copy(userId = "sampleUser", teamId = "sampleTeam", channelId = "sampleChannel")


    }


    @DisplayName("Test Trigger Standup with open standups")
    @Test
    fun testTriggerOpenStandups() {
        slackClient.chat().postEphemeral("").successResponse = SuccessfulPostEphemeralResponse.sample()
        standupRepository.insert(Standup.sample().copy(userId = command.userId, teamId = command.teamId, status = Standup.Status.OPEN, date = LocalDate.now()))

        triggerStandupDialog = TriggerStandupDialog(slackClient, standupRepository, mock())
        triggerStandupDialog.openDialog(command, "")
        val expectedParam = PostEphemeralRequest(text = "Sorry, I can't start this standup for you, yet. You still have an active standup going on.",
                channel = "sampleChannel", user = "sampleUser")
        Assertions.assertEquals(slackClient.chat().postEphemeral("").params(), expectedParam)
    }

    @DisplayName("Test Trigger Standup without standups")
    @Test
    fun testTrigger() {
        slackClient.dialog().open("").successResponse = SuccessfulOpenDialogResponse.sample()

        standupRepository.insert(Standup.sample().copy(userId = command.userId, teamId = command.teamId,
                status = Standup.Status.CREATED, date = LocalDate.now(), name = "sampleStandup", standupDefinitionId = "sampleSD"))
        triggerStandupDialog = TriggerStandupDialog(slackClient, standupRepository, mock())
        triggerStandupDialog.openDialog(command, "")

        val expectedParam = SlackOpenDialogRequest(trigger_id = command.triggerId,
                dialog = Dialog(
                        callback_id = TriggerStandupDialog.CALLBACK_ID,
                        title = "Trigger a Stand-up",
                        elements = listOf(
                                SelectElement(label = "Select the stand-up",
                                        name = TriggerStandupDialog.STANDUP_SELECTION,
                                        options = listOf(Options("sampleStandup", "sampleSD")),
                                        type = Type.SELECT)
                        )))
        Assertions.assertEquals(slackClient.dialog().open("").params(), expectedParam)
    }
}