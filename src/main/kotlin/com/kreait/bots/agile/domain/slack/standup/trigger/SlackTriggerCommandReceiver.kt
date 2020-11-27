package com.kreait.bots.agile.domain.slack.standup.trigger

import com.kreait.bots.agile.domain.common.data.Command
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder

import com.kreait.slack.broker.receiver.SlashCommandReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.SlackCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class SlackTriggerCommandReceiver @Autowired constructor(private val triggerStandupDialog: TriggerStandupDialog) : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.commands.trigger")
                .description("Total number of executed trigger commands")
                .register(registry)
    }

    /**
     * receives the trigger-standup slackcommand
     */
    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        this.counter?.increment()
        triggerStandupDialog.openDialog(slackCommand, team.bot.accessToken)
    }

    /**
     * checks if the executed command is the trigger-standup- command
     */
    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        return (slackCommand.command.startsWith("/trigger-standup") || (slackCommand.command.startsWith("/olaph") && slackCommand.text.trim().toLowerCase() == Command.TRIGGER))
    }
}
