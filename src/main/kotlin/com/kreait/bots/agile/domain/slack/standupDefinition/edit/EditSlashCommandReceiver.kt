package com.kreait.bots.agile.domain.slack.standupDefinition.edit

import com.kreait.bots.agile.domain.common.data.Command
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.select.PostEditStandupSelectionService
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
class EditSlashCommandReceiver @Autowired constructor(private val postSelectStandupQuestion: PostEditStandupSelectionService) : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        counter = Counter.builder("olaph.commands.edit")
                .description("Total number of edit commands")
                .register(registry)
    }

    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        this.counter?.increment()
        this.postSelectStandupQuestion.postSelectStandupQuestion(slackCommand, team.bot.accessToken)
    }

    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        //TODO Change before going live, currently needed because of different configurations
        return (slackCommand.command.startsWith("/edit-standup") || (slackCommand.command.startsWith("/olaph") && slackCommand.text.trim().toLowerCase() == Command.EDIT))
    }

}


