package com.kreait.bots.agile.domain.slack.standupDefinition.list

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
class ListCommandReceiver @Autowired constructor(val listStandupsService: ListStandupsService) : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.commands.leave")
                .description("Total number of executed leave commands")
                .register(registry)
    }

    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        this.counter?.increment()
        listStandupsService.listStandups(slackCommand, team.bot.accessToken)
    }

    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        return (slackCommand.command.startsWith("/list-standups") || (slackCommand.command.startsWith("/olaph") && slackCommand.text.trim().toLowerCase() == Command.LIST))
    }
}
