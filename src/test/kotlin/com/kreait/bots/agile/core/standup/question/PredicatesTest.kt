package com.kreait.bots.agile.core.standup.question

import com.kreait.bots.agile.core.standup.common.example
import com.kreait.bots.agile.domain.common.data.Standup
import com.kreait.bots.agile.domain.common.data.Standup.Status
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class PredicatesTest {

    private val question1 = "question1"
    private val qiestion2 = "question2"

    @Nested
    inner class TestStatus {

        @ParameterizedTest
        @EnumSource(value = Status::class, names = ["CREATED", "CLOSED", "CANCELLED"])
        @DisplayName("Question can't be send due to wrong Status")
        fun testWrongStatus(status: Status) {
            Assertions.assertFalse(Standup.Predicates.canSendQuestion(Standup.example(status = status, questions = listOf(question1))))
        }

        @ParameterizedTest
        @EnumSource(value = Status::class, names = ["OPEN"])
        @DisplayName("Question can be send with correct Status")
        fun testCorrectStatus(status: Status) {
            Assertions.assertTrue(Standup.Predicates.canSendQuestion(Standup.example(status = status, questions = listOf(question1))))
        }

    }

    @Nested
    inner class TestQuestionsSend {

        @DisplayName("Question can't be send")
        @Test
        fun testWrongNumber() {
            Assertions.assertFalse(Standup.Predicates.canSendQuestion(Standup.example(status = Status.OPEN,
                    questions = listOf(question1), questionsAsked = 2)))
        }

        @DisplayName("Question can be send")
        @Test
        fun testCorrectNumber() {
            Assertions.assertTrue(Standup.Predicates.canSendQuestion(Standup.example(status = Status.OPEN,
                    questions = listOf(question1, qiestion2), questionsAsked = 1, answers = listOf(Standup.Answer.example()))))
        }
    }

    @Nested
    inner class TestQuestions {

        @DisplayName("Question can't be send: question list is empty")
        @Test
        fun testWrongQuestionListEmpty() {
            Assertions.assertFalse(Standup.Predicates.canSendQuestion(Standup.example(status = Status.OPEN)))
        }

        @DisplayName("Question can be send")
        @Test
        fun testCorrectQuestionListEmpty() {
            Assertions.assertTrue(Standup.Predicates.canSendQuestion(Standup.example(status = Status.OPEN, questions = listOf(question1, qiestion2))))
        }
    }

    @Nested
    inner class TestQuestionsAsked {

        @DisplayName("Question can not be send: questionsAsked is not lower than number of questions")
        @Test
        fun testWrongQuestionsAsked() {
            Assertions.assertFalse(Standup.Predicates.canSendQuestion(Standup.example(questions = listOf(question1, qiestion2),
                    status = Status.OPEN, questionsAsked = 2, answers = listOf(Standup.Answer.example(), Standup.Answer.example()))))
        }

        @DisplayName("Question can be send: questionsAsked is lower than number of Questions")
        @Test
        fun testCorrectQuestionsAsked() {
            Assertions.assertTrue(Standup.Predicates.canSendQuestion(Standup.example(questions = listOf(question1, qiestion2),
                    status = Status.OPEN, questionsAsked = 1, answers = listOf(Standup.Answer.example()))))
        }
    }

}
