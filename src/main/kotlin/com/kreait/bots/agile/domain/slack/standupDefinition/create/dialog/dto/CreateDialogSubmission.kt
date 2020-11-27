package com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.kreait.bots.agile.core.configuration.converter.StringToLocalTimeConverter
import com.kreait.slack.api.contract.jackson.group.dialog.DialogValidationError
import com.kreait.slack.api.contract.jackson.util.JacksonDataClass
import com.kreait.slack.broker.exception.DialogValidationException
import java.time.DayOfWeek
import java.time.LocalTime

@JacksonDataClass
data class CreateDialogSubmission(
        @JsonProperty(NAME) val name: String,
        @JsonProperty(DAYS) @JsonDeserialize(using = DayOfWeekDeserializer::class) val days: List<DayOfWeek>,
        @JsonProperty(TIME) @JsonDeserialize(using = LocalTimeDeserializer::class) val time: LocalTime,
        @JsonProperty(BROADCAST_CHANNEL_ID) val broadcastChannelId: String,
        @JsonProperty(QUESTIONS) @JsonDeserialize(using = StringToStringListDeserializer::class) val questions: List<String>,
        @JsonProperty(STATE) val state: String?) {

    companion object {
        const val NAME = "standupName"
        const val DAYS = "standupDays"
        const val TIME = "standupTime"
        const val BROADCAST_CHANNEL_ID = "standupBroadcastChannelId"
        const val QUESTIONS = "standupQuestions"
        const val STATE = "state"

        private val textDaysOfWeek = mapOf(
                "mon" to DayOfWeek.MONDAY,
                "tue" to DayOfWeek.TUESDAY,
                "wed" to DayOfWeek.WEDNESDAY,
                "thu" to DayOfWeek.THURSDAY,
                "fri" to DayOfWeek.FRIDAY,
                "sat" to DayOfWeek.SATURDAY,
                "sun" to DayOfWeek.SUNDAY)

        private val objectMapper = ObjectMapper()

        fun of(map: Map<String, Any>): CreateDialogSubmission {
            val createDialogSubmission = objectMapper.convertValue(map, CreateDialogSubmission::class.java)
            validate(createDialogSubmission)
            return createDialogSubmission
        }

        private fun validate(createDialogSubmission: CreateDialogSubmission) {
            if (createDialogSubmission.name.isBlank())
                throw DialogValidationException(listOf(DialogValidationError(NAME, "Invalid input, stand-up name can\'t be blank")))
        }
    }

    class DayOfWeekDeserializer : JsonDeserializer<List<DayOfWeek>>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?) = p.text.toLowerCase().split(" ")
                .mapNotNull {
                    if (textDaysOfWeek.containsKey(it)) {
                        textDaysOfWeek[it]
                    } else {
                        throw DialogValidationException(listOf(DialogValidationError(DAYS, "Invalid input, did you spell it right?")))
                    }
                }
    }

    /**
     * Deserializer that creates a list of strings from a string with newlines
     * - without empty strings
     * - without leading or tailing white spaces
     * - starting with a capital letter
     */
    class StringToStringListDeserializer : JsonDeserializer<List<String>>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?) = p.text.split("\n")
                .filterNot { it.isEmpty() }
                .map { it.trim().capitalize() }

    }

    // TODO this converter could be moved to a more generic package
    class LocalTimeDeserializer : JsonDeserializer<LocalTime>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?) = StringToLocalTimeConverter.INSTANCE.convert(p.text)
    }
}
