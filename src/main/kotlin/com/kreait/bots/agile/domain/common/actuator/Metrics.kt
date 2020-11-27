package com.kreait.bots.agile.domain.common.actuator

import com.fasterxml.jackson.annotation.JsonProperty


data class Metrics(val workspaces: String, val members: String, val questions: String, val standups: String)


data class ArrayCount(
        @JsonProperty("_id") val id: String = "",
        @JsonProperty("count") val count: Int = 0
)