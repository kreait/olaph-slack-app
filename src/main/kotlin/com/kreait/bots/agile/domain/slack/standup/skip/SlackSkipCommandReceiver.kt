package com.kreait.bots.agile.domain.slack.standup.skip

import com.kreait.bots.agile.core.standup.open.StandupOpeningService
import com.kreait.bots.agile.domain.common.data.Command
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import com.kreait.slack.broker.receiver.SlashCommandReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.SlackCommand
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class SlackSkipCommandReceiver @Autowired constructor(private val standupRepository: StandupRepository,
                                                      private val skipMessageSender: SkipMessageSender,
                                                      private val standupOpeningService: StandupOpeningService) : SlashCommandReceiver, MeterBinder {

    private var counter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.commands.skip")
                .description("Total number of executed skip commands")
                .register(registry)
    }

    /**
     * cancels the open standup, sends info message & opens the next standup
     */
    override fun onReceiveSlashCommand(slackCommand: SlackCommand, headers: HttpHeaders, team: Team) {
        this.counter?.increment()
        val openStandup = standupRepository.find(withUserIds = setOf(slackCommand.userId), withStatus = setOf(Standup.Status.OPEN))
        if (openStandup.isNotEmpty()) {
            standupRepository.update(withIds = setOf(openStandup.first().id!!), update = Update.update(Standup.STATUS, Standup.Status.CANCELLED))
            skipMessageSender.sendSuccesfulSkipMessage(slackCommand, openStandup.first().name, team.bot.accessToken)
            standupOpeningService.openStandup(slackCommand.userId, slackCommand.teamId)
        } else {
            skipMessageSender.sendNoStandupsFoundMessage(slackCommand, team.bot.accessToken)
        }
    }

    /**
     * @returns [true] when the called command is the skip command
     */
    override fun supportsCommand(slackCommand: SlackCommand): Boolean {
        return (slackCommand.command.startsWith("/skip-standup") || (slackCommand.command.startsWith("/olaph") && slackCommand.text.trim().toLowerCase() == Command.SKIP))
    }
}
