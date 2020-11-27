package com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open

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
class LeaveSlashCommandHandler @Autowired constructor(private val leaveDialogOpeningService: LeaveDialogOpeningService,
                                                      private val standupDefinitionRepository: StandupDefinitionRepository,
                                                      private val userService: UserService,
                                                      private val messageSender: SlackMessageSender) {

    companion object {
        private val LOG = LoggerFactory.getLogger(LeaveSlashCommandHandler::class.java)
    }

    fun handleLeaveSlashCommand(slackCommand: SlackCommand, accessToken: String) {
        val userId = slackCommand.userId
        val teamId = slackCommand.teamId

        val options = leaveOptions(teamId, userId, accessToken)

        if (options.isNotEmpty()) {
            this.leaveDialogOpeningService.openStandupLeaveDialog(options, slackCommand.triggerId, slackCommand.userId, accessToken)
        } else {
            this.messageSender.sendEphemeralMessage(ResponseType.NO_STANDUPS_FOUND, messageContext = MessageContext.of(slackCommand), token = accessToken)
        }
    }

    /**
     * Returns [List<Options>] with Standups names where user is part of the standup and for given Team/Workspace
     */
    private fun leaveOptions(teamId: String, userId: String, accessToken: String): List<Options> =
            this.standupDefinitionRepository.find(
                    withBroadcastChannels = userService.conversationList(accessToken, userId).map { it.id }.toSet(),
                    withTeamId = teamId,
                    withUserId = userId,
                    withStatus = setOf(StandupDefinition.Status.ACTIVE)
            ).map { it ->
                Options(if (it.name.length > 75) "${it.name.substring(0, 71)}..." else it.name, it.id!!)
            }
}
