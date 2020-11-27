package com.kreait.bots.agile.core.standup.close

import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import com.kreait.bots.agile.domain.common.service.SlackMessageSender
import com.kreait.bots.agile.domain.response.ResponseType
import com.kreait.slack.broker.store.team.TeamStore
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class StandupCancelService @Autowired constructor(private val standupRepository: StandupRepository,
                                                  private val messageSender: SlackMessageSender,
                                                  private val teamStore: TeamStore) : MeterBinder {
    companion object {

        private val Log = LoggerFactory.getLogger(StandupCancelService::class.java)
    }

    private var counter: Counter? = null

    override fun bindTo(registry: MeterRegistry) {
        this.counter = Counter.builder("olaph.standups.expired")
                .description("Total number of expired stand-ups")
                .register(registry)
    }

    /**
     * Cancels outdated (unfinished and older than 1 day) standups
     */
    fun cancelOutdatedStandups() {
        val limit = 500
        var offset = 0L
        do {
            val standups = this.standupRepository.find(withStatus = setOf(Standup.Status.OPEN), timestampIsBefore = Instant.now().minus(Duration.ofHours(23)),
                    offset = offset, limit = limit)
            standups.forEach {
                try {
                    cancelStandup(it)
                    this.messageSender.sendMessage(ResponseType.EXPIRATION_MESSAGE,
                            messageContext = MessageContext(date = it.date, currentStandup = it.name, teamId = it.teamId, userId = it.userId), token = teamStore.findById(it.teamId).bot.accessToken)
                    this.counter?.increment()
                } catch (e: Exception) {
                    Log.error("Error during standup cancellation for $it", e)
                }
            }
            if (standups.size < limit) {
                break
            } else {
                offset += limit.toLong()
            }
            Log.debug("Found $offset standups")
        } while (standups.size == limit)

    }

    private fun cancelStandup(standup: Standup) {
        standup.id?.let { id ->
            this.standupRepository.update(withIds = setOf(id), update = Update.update(Standup.STATUS, Standup.Status.CANCELLED))
        }
    }
}
