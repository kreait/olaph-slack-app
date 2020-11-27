package com.kreait.bots.agile.domain.slack.standup

import com.kreait.bots.agile.core.standup.answer.StandupAnswerReceiver
import com.kreait.slack.broker.receiver.EventReceiver
import com.kreait.slack.broker.store.team.Team
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class SlackAnswerReceiver @Autowired constructor(private val answerReceiver: StandupAnswerReceiver) : EventReceiver {

    /**
     * supports [slackEvent] which are triggered by user-messages.
     * @return whether the event is supported or not
     */
    override fun supportsEvent(slackEvent: SlackEvent): Boolean {
        return slackEvent.event.containsKey("client_msg_id") &&
                slackEvent.event.containsKey("user") &&
                slackEvent.event["type"] == "message" &&
                slackEvent.event["channel_type"] == "im"

    }

    override fun onReceiveEvent(slackEvent: SlackEvent, headers: HttpHeaders, team: Team) {
        val answer = slackEvent.event["text"] as String
        val userId = slackEvent.event["user"] as String

        answerReceiver.handleAnswer(userId = userId, answer = answer, eventId = slackEvent.eventId, eventTime = slackEvent.eventTime, teamId = slackEvent.teamId)
    }

}
