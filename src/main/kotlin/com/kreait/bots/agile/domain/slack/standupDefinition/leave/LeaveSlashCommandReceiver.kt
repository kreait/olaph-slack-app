package com.kreait.bots.agile.domain.slack.standupDefinition.leave

import com.kreait.bots.agile.domain.common.data.Command
import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.help.SlackLeaveHelpMessageService
import com.kreait.bots.agile.domain.slack.standupDefinition.leave.dialog.open.LeaveSlashCommandHandler
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import com.kreait.slack.broker.receiver.SlashCommandReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.SlackCommand
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service


@Service
class LeaveSlashCommandReceiver(private val leaveSlashCommandHandler: LeaveSlashCommandHandler,
                                private val slackLeaveHelpMessageService: SlackLeaveHelpMessageService) : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null
    private var helpCounter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.commands.leave")
                .description("Total number of executed leave commands")
                .register(registry)

        this.helpCounter = Counter.builder("olaph.commands.leave-help")
                .description("Total number of executed leave help commands")
                .register(registry)
    }

    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        if (slackCommand.text.contains("help")) {
            this.helpCounter?.increment()
            slackLeaveHelpMessageService.sendHelpMessage(slackCommand, team.bot.accessToken)
        } else {
            this.counter?.increment()
            this.leaveSlashCommandHandler.handleLeaveSlashCommand(slackCommand, team.bot.accessToken)
        }
    }

    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        return (slackCommand.command.startsWith("/leave-standup") || (slackCommand.command.startsWith("/olaph") && slackCommand.text.trim().toLowerCase() == Command.LEAVE))
    }
}
