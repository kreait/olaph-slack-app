package com.kreait.bots.agile.domain.slack.standupDefinition.create

import com.kreait.bots.agile.domain.common.data.Command
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.open.CreateDialogOpeningService
import com.kreait.bots.agile.domain.slack.standupDefinition.create.help.SlackCreationHelpMessageService
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
open class CreateSlashCommandReceiver @Autowired constructor(private val createDialogOpeningService: CreateDialogOpeningService,
                                                             private val slackCreationHelpMessageService: SlackCreationHelpMessageService)
    : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null
    private var helpCounter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.commands.create")
                .description("Total number of executed create commands")
                .register(registry)


        this.helpCounter = Counter.builder("olaph.commands.create-help")
                .description("Total number of executed create help commands")
                .register(registry)
    }

    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        if (slackCommand.text.contains("help")) {
            this.helpCounter?.increment()
            slackCreationHelpMessageService.sendHelpMessage(slackCommand, team.bot.accessToken)
        } else {
            this.counter?.increment()
            this.createDialogOpeningService.openCreationDialog(slackCommand.triggerId, slackCommand.userId, team.bot.accessToken)
        }
    }

    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        return (slackCommand.command.startsWith("/create-standup") ||
                (slackCommand.command.startsWith("/olaph") && slackCommand.text.trim().toLowerCase() == Command.CREATE))
    }
}
