package com.kreait.bots.agile.domain.v2.service

import com.kreait.bots.agile.domain.v2.data.Questionnaire
import com.kreait.bots.agile.domain.v2.data.StandupSpec
import com.kreait.bots.agile.domain.v2.repository.QuestionnaireRepository
import com.kreait.bots.agile.domain.v2.repository.StandupSpecRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Creates [Questionnaire]s
 */
@Service
class QuestionnaireCreationService @Autowired constructor(private val standupSpecRepository: StandupSpecRepository,
                                                          private val questionnaireRepository: QuestionnaireRepository) {

    /**
     * Creates and saves [Questionnaire]s from [StandupSpec]s for the current day. To do so it...
     * - Gets all [StandupSpec]s for Stand-Ups that shall happen on the current weekday
     * - For each of these [StandupSpec]s it creates a [Questionnaire], unless one already exists for that user
     */
    fun createQuestionnaires() {
        standupSpecRepository
                .find(onDayOfWeek = LocalDate.now().dayOfWeek)
                ?.forEach { spec ->
                    spec.participants.filterNot {
                        this.questionnaireRepository.exists(withStandupSpecId = spec.id, withUserId = it.userId, withDate = LocalDate.now())
                    }.forEach { user ->
                        questionnaireRepository.save(
                                Questionnaire.of(
                                        spec,
                                        user.userId,
                                        getCurrentDay(spec, LocalDate.now().dayOfWeek)
                                )
                        )
                    }
                }
    }

    private fun getCurrentDay(spec: StandupSpec, dayOfWeek: DayOfWeek): StandupSpec.Day {
        return spec.days.first {
            it.dayOfWeek == dayOfWeek
        }
    }
}
