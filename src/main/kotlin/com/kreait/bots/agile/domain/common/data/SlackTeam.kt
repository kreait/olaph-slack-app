package com.kreait.bots.agile.domain.common.data

import com.kreait.slack.api.contract.jackson.group.oauth.SuccessfullAccessResponse
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = SlackTeam.COLLECTION_NAME)
data class SlackTeam(@Id @Field(TEAM_ID) val teamId: String,
                     @Field(TEAM_NAME) val teamName: String,
                     @Field(INCOMING_WEBHOOK) val incomingWebhook: IncomingWebhook,
                     @Field(BOT) val bot: Bot,
                     @Field(STATUS) var status: Status = Status.ACTIVE,
                     @Field(INSTALLED_AT) var installedAt: Instant?) {
    data class Bot(
            @Field(BOT_USER_ID) val userId: String,
            @Field(BOT_ACCESS_TOKEN) val accessToken: String
    )

    data class IncomingWebhook(
            @Field(CHANNEL) val channel: String,
            @Field(CHANNEL_ID) val channelId: String,
            @Field(CONFIGURATION_URL) val configurationUrl: String,
            @Field(URL) val url: String
    )

    companion object {
        fun of(oauthResponse: SuccessfullAccessResponse): SlackTeam {
            return SlackTeam(teamId = oauthResponse.teamId, teamName = oauthResponse.teamName,
                    incomingWebhook = IncomingWebhook(channel = oauthResponse.incomingWebhook.channel,
                            channelId = oauthResponse.incomingWebhook.channelId, configurationUrl = oauthResponse.incomingWebhook.configurationUrl, url = oauthResponse.incomingWebhook.url),
                    bot = Bot(userId = oauthResponse.bot.botUserId, accessToken = oauthResponse.bot.botAccessToken), status = Status.ACTIVE, installedAt = Instant.now())
        }

        const val COLLECTION_NAME = "slackteams"
        const val TEAM_ID = "_id"
        const val TEAM_NAME = "team_name"
        const val INCOMING_WEBHOOK = "incoming_webhook"
        const val BOT = "bot"
        const val BOT_USER_ID = "bot_user_id"
        const val BOT_ACCESS_TOKEN = "bot_access_token"
        const val CHANNEL = "channel"
        const val CHANNEL_ID = "channel_id"
        const val CONFIGURATION_URL = "configuration_url"
        const val URL = "url"
        const val STATUS = "status"
        const val INSTALLED_AT = "installedAt"

    }

    enum class Status { ACTIVE, ARCHIVED }
}
