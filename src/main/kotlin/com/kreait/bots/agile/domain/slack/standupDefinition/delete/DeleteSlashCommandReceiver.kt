package com.kreait.bots.agile.domain.slack.standupDefinition.delete

import com.kreait.bots.agile.domain.common.data.Command
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.dialog.open.DeleteSlashCommandHandler
import com.kreait.bots.agile.domain.slack.standupDefinition.delete.help.SlackDeletionHelpMessageService
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import com.kreait.slack.broker.receiver.SlashCommandReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.SlackCommand
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
open class DeleteSlashCommandReceiver(private val deleteSlashCommandHandler: DeleteSlashCommandHandler, private val slackDeletionHelpMessageService: SlackDeletionHelpMessageService)
    : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null
    private var helpCounter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.commands.delete")
                .description("Total number of executed delete commands")
                .register(registry)

        this.helpCounter = Counter.builder("olaph.commands.delete-help")
                .description("Total number of executed delete help commands")
                .register(registry)
    }

    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        if (slackCommand.text.contains("help")) {
            this.helpCounter?.increment()
            this.slackDeletionHelpMessageService.sendHelpMessage(slackCommand, team.bot.accessToken)
        } else {
            this.counter?.increment()
            this.deleteSlashCommandHandler.handleDeleteSlashCommand(slackCommand, team.bot.accessToken)
        }
    }

    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        return (slackCommand.command.startsWith("/delete-standup") || (slackCommand.command.startsWith("/olaph") && slackCommand.text.trim().toLowerCase() == Command.DELETE))
    }
}
