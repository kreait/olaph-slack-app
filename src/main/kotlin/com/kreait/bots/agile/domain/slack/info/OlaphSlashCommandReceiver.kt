package com.kreait.bots.agile.domain.slack.info

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
open class OlaphSlashCommandReceiver @Autowired constructor(private val slackSendInfoService: SlackSendInfoService)
    : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.commands.help")
                .description("Total number of executed help commands")
                .register(registry)
    }

    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        this.counter?.increment()
        this.slackSendInfoService.sendInfoMessage(slackCommand, team.bot.accessToken)
    }

    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        //TODO Change before going live, currently needed because of different configurations
        return (slackCommand.command.startsWith("/olaph") && (slackCommand.text.trim().toLowerCase() == Command.HELP || slackCommand.text.trim().toLowerCase() == ""))
    }
}
