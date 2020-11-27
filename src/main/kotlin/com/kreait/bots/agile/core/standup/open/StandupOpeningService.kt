package com.kreait.bots.agile.core.standup.open

import com.kreait.bots.agile.core.standup.question.QuestionService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.Standup.Status
import com.kreait.bots.agile.domain.common.data.StandupRepository
import com.kreait.bots.agile.domain.slack.standup.SlackOpeningMessageSender
import com.kreait.bots.agile.domain.slack.standup.reminder.ReminderService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

/**
 * Opens Standups and sends opening messages
 */
@Service
class StandupOpeningService @Autowired constructor(private val questionService: QuestionService,
                                                   private val openingMessageSender: SlackOpeningMessageSender,
                                                   private val standupRepository: StandupRepository,
                                                   private val reminderService: ReminderService) {
    companion object {
        val Log = LoggerFactory.getLogger(StandupOpeningService::class.java)
    }

    /**
     * checks [canTransitionToOpen] and
     * changes the [Status] of [Standup]s to [Status.OPEN]
     */
    fun findAndOpenStandups() {
        val limit = 500
        var offset = 0L

        do {
            val currentStandups = this.standupRepository.find(withStatus = setOf(Status.CREATED), timestampIsAfter = Instant.now().minus(Duration.ofHours(23)), limit = limit, offset = offset)
            val groupedStandups = currentStandups.sortedBy { it.time }.groupBy { it.userId } // group by user
                    .map { it.value.first() } // pick the first standup per user
            when {
                Log.isDebugEnabled -> {
                    Log.debug("found ${currentStandups.size} created standups for ${groupedStandups.size} different users")
                }
            }
            groupedStandups.filter { Standup.Predicates.canTransitionToOpen(it) }
                    .forEach { standup ->
                        try {
                            val openStandup = this.standupRepository.find(withUserIds = setOf(standup.userId), withStatus = setOf(Status.OPEN))
                            if (openStandup.isNotEmpty()) {
                                if (!standup.reminded) {
                                    reminderService.sendReminder(standup, openStandup.first())
                                    standupRepository.update(withIds = setOf(standup.id!!), update = Update.update(Standup.REMINDED, true))
                                }
                            } else {
                                this.standupRepository.update(withIds = setOf(standup.id!!), update = Update.update(Standup.STATUS, Standup.Status.OPEN))
                                val response = openingMessageSender.sendOpeningMessage(standup = standup)
                                if (response.success) {
                                    this.questionService.sendQuestions(standup.userId)
                                    when {
                                        Log.isDebugEnabled -> {
                                            Log.debug("opening message was sent successfully")
                                        }
                                    }
                                } else {
                                    if (response.errorCode == "account_inactive") {
                                        this.standupRepository.update(withIds = setOf(standup.id), update = Update.update(Standup.STATUS, Standup.Status.CANCELLED))
                                    }
                                    when {
                                        Log.isDebugEnabled -> {
                                            Log.debug("Failed to send opening-message: $response")
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.error("Error during standup opening for $standup ", e)
                        }
                    }
            offset += limit
        } while (currentStandups.size == limit)
    }


    /**
     * opens the next standup of a user
     * [userId] the id of the user for which the next standup should be updated
     */
    fun openStandup(userId: String, teamId: String) {
        if (!this.standupRepository.exists(withUserIds = setOf(userId), withStatus = setOf(Status.OPEN))) {
            val standup = this.standupRepository.find(withUserIds = setOf(userId), withStatus = setOf(Status.CREATED), timestampIsBefore = Instant.now())
                    .firstOrNull()
            standup?.let {
                this.standupRepository.update(withIds = setOf(it.id!!), update = Update.update(Standup.STATUS, Standup.Status.OPEN))

                val response = openingMessageSender.sendOpeningMessage(standup = it)
                if (response.success) {
                    this.questionService.sendQuestions(it.userId)
                } else {
                    if (response.errorCode == "account_inactive") {
                        this.standupRepository.update(withIds = setOf(it.id), update = Update.update(Standup.STATUS, Standup.Status.CANCELLED))
                    }
                }
            }
        }
    }
}
