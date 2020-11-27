package com.kreait.bots.agile.domain.slack.standupDefinition.join

import com.kreait.bots.agile.domain.common.data.Command
import com.kreait.bots.agile.domain.slack.standupDefinition.join.dialog.open.JoinSlashCommandHandler
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import com.kreait.slack.broker.receiver.SlashCommandReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.SlackCommand
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service


@Service
class JoinSlashCommandReceiver(private val joinSlashCommandHandler: JoinSlashCommandHandler) : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.commands.join")
                .description("Total number of executed join commands")
                .register(registry)
    }

    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        this.counter?.increment()
        this.joinSlashCommandHandler.handleJoinSlashCommand(slackCommand, team.bot.accessToken)
    }

    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        return (slackCommand.command.startsWith("/join-standup") || (slackCommand.command.startsWith("/olaph") && slackCommand.text.trim().toLowerCase() == Command.JOIN))
    }
}
