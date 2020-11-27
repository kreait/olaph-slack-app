package com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.open

import com.kreait.bots.agile.domain.common.data.StandupDefinition
import com.kreait.bots.agile.domain.common.data.StandupDefinitionRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.api.contract.jackson.SlackCommand
import com.kreait.slack.api.contract.jackson.group.dialog.Options
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DeleteSlashCommandHandler @Autowired constructor(private val deleteDialogOpeningService: DeleteDialogOpeningService,
                                                       private val standupDefinitionRepository: StandupDefinitionRepository,
                                                       private val userService: UserService,
                                                       private val slackMessageSender: SlackMessageSender) {

    companion object {
        private val LOG = LoggerFactory.getLogger(DeleteSlashCommandHandler::class.java)
    }

    /**
     * Handles the invocation of the delete slash command
     * Decides wether to open deletion dialog or sending message that no standups where found
     * @param slackCommand slackCommand that triggered the deletion
     */
    fun handleDeleteSlashCommand(slackCommand: SlackCommand, accessToken: String) {
        val options = createDeleteOptions(slackCommand.teamId, slackCommand.userId, accessToken)
        if (options.isNotEmpty()) {
            this.deleteDialogOpeningService.openStandupDeletionDialog(options, slackCommand.triggerId, slackCommand.userId, accessToken)
        } else {
            this.slackMessageSender.sendEphemeralMessage(
                    ResponseType.NO_STANDUPS_FOUND,
                    messageContext = MessageContext.of(slackCommand), token = accessToken)
        }
    }

    /**
     * Fetches the conversations and prepares them for the Dialog
     * @return list of channel options
     */
    private fun createDeleteOptions(teamId: String, userId: String, accessToken: String): List<Options> {
        return this.standupDefinitionRepository.find(
                withStatus = setOf(StandupDefinition.Status.ACTIVE),
                withTeamId = teamId,
                withBroadcastChannels = userService.conversationList(accessToken, userId).map { it.id }.toSet()
        ).map { it ->
            Options(
                    label =
                    if (it.name.length > 75)
                        "${it.name.substring(0, 71)}..."
                    else it.name,
                    value =
                    it.id!!
            )
        }
    }

}
