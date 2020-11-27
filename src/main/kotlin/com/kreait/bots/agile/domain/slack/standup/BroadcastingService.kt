package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.core.standup.open.StandupOpeningService
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.StandupRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class BroadcastingService @Autowired constructor(private val standupRepository: StandupRepository,
                                                 private val standupOpeningService: StandupOpeningService,
                                                 private val broadcastSender: SlackBroadcastSender) {

    //TODO i think we should think about the way we broadcast messages because this might concurrency issues
    //Do we still need this when we have the [StandupAnswerReceiver]?
    /**
     * Finds an open Question for the userId and triggers sending
     */
    fun broadcast(userId: String? = null) {
        this.standupRepository.findAnsweredAndOpen(userId)
                .forEach { standup ->
                    this.sendUserConfirmation(standup.userId, standup.teamId)

                    this.sendBroadcast(standup)

                    this.standupRepository.update(withIds = setOf(standup.id!!), update = Update.update(Standup.STATUS, Standup.Status.CLOSED))

                    this.standupOpeningService.openStandup(standup.userId, standup.teamId)
                }
    }

    private fun sendUserConfirmation(userId: String, teamId: String) {
        this.broadcastSender.sendBroadcastConfirmation(userId, teamId)
    }

    private fun sendBroadcast(standup: Standup) {
        broadcastSender.sendBroadcast(userId = standup.userId,
                standupName = standup.name,
                date = standup.date,
                questions = standup.questions,
                broadcastChannelId = standup.broadcastChannelId,
                answers = standup.answers.map { it.text },
                teamId = standup.teamId)
    }
}
