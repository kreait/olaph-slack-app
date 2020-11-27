package com.kreait.bots.agile.domain.v2

import com.kreait.bots.agile.domain.v2.data.Questionnaire
import com.kreait.bots.agile.domain.v2.data.StandupSpec
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId


fun Questionnaire.Companion.sample() =
        Questionnaire(null,
                "",
                Questionnaire.LifecycleStatus.CREATED,
                LocalDate.now(),
                LocalTime.now(),
                ZoneId.systemDefault(),
                Questionnaire.TimezoneSource.STANDUP_SPEC,
                Instant.now(),
                "",
                "",
                listOf(Questionnaire.Item(question = Questionnaire.Item.Question(Questionnaire.Item.Question.Type.TEXT, ""))),
                0, listOf(""))

fun StandupSpec.Participant.Companion.sample() =
        StandupSpec.Participant("someId", ZoneId.systemDefault(), "")

fun StandupSpec.Companion.sample() =
        StandupSpec("someId", "",
                StandupSpec.Status.ACTIVE,
                listOf(StandupSpec.Participant.sample()),
                listOf(StandupSpec.Question(StandupSpec.Question.Type.TEXT, "")),
                ZoneId.systemDefault(),
                listOf(
                        StandupSpec.Day(DayOfWeek.MONDAY, LocalTime.of(1, 0)),
                        StandupSpec.Day(DayOfWeek.TUESDAY, LocalTime.of(1, 0)),
                        StandupSpec.Day(DayOfWeek.WEDNESDAY, LocalTime.of(1, 0)),
                        StandupSpec.Day(DayOfWeek.THURSDAY, LocalTime.of(1, 0)),
                        StandupSpec.Day(DayOfWeek.FRIDAY, LocalTime.of(1, 0))
                ),
                listOf(""),
                "",
                ""
        )

fun Questionnaire.Item.Question.Companion.sample() = Questionnaire.Item.Question(Questionnaire.Item.Question.Type.TEXT, "")

fun Questionnaire.Item.Answer.Companion.sample() = Questionnaire.Item.Answer("")

fun Questionnaire.Item.Companion.sample() = Questionnaire.Item(question = Questionnaire.Item.Question.sample(), answer = Questionnaire.Item.Answer.sample())