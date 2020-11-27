package com.kreait.bots.agile.domain.slack.standup.statistics

import com.kreait.bots.agile.core.standup.answer.StandupAnswerReceiver
import com.kreait.slack.api.SlackClient
import com.kreait.slack.api.contract.jackson.event.SlackEvent
import com.kreait.slack.api.contract.jackson.group.chat.PostMessageRequest
import com.kreait.slack.broker.receiver.EventReceiver
import com.kreait.slack.broker.store.team.Team
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class SlackStatReceiver @Autowired constructor(private val answerReceiver: StandupAnswerReceiver,
                                               @Value("\${olaph.admin.workspaceid}") private val workspaceId: String,
                                               @Value("\${olaph.admin.secret}") private val adminSecret: String,
                                               private val statisticsController: StatistikController,
                                               private val slackClient: SlackClient) : EventReceiver {

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

    override fun shouldThrowException(exception: Exception): Boolean =
            true

    override fun onReceiveEvent(slackEvent: SlackEvent, headers: HttpHeaders, team: Team) {
        val answer = slackEvent.event["text"] as String
        val userId = slackEvent.event["user"] as String
        if (team.teamId == workspaceId && answer.contains(adminSecret)) {
            val segments = answer.split(" ")
            val format = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val result = statisticsController.getDauforMonths(LocalDate.parse(segments[1], format), LocalDate.parse(segments[2], format))
            this.slackClient.chat().postMessage(team.bot.accessToken)
                    .with(PostMessageRequest("Active users in defined range: $result", channel = slackEvent.event["channel"].toString()))
                    .onSuccess { println(it) }
                    .onFailure { println(it) }
                    .invoke()
        }

        answerReceiver.handleAnswer(userId = userId, answer = answer, eventId = slackEvent.eventId, eventTime = slackEvent.eventTime, teamId = slackEvent.teamId)
    }

}
