package com.kreait.bots.agile.domain.slack.standup.statistics

import com.fasterxml.jackson.annotation.JsonProperty


data class DailyActiveUsers(@get:JsonProperty("amount")
                            @set:JsonProperty("amount") var amount: String)