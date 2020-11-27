package com.kreait.bots.agile.domain.response

import com.kreait.bots.agile.TestApplication
import com.kreait.bots.agile.domain.common.data.RandomResponses
import com.kreait.bots.agile.domain.common.data.RandomResponsesRepository
import com.kreait.bots.agile.domain.common.service.MessageContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.query.Update
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [(TestApplication::class)])
class RandomResponseProviderTest @Autowired constructor(private val randomResponsesRepository: RandomResponsesRepository) {

    @DisplayName("Test Random Response Provider")
    @Test
    fun testRandomAnswerProvider() {

        randomResponsesRepository.update(ResponseType.REMINDER_MESSAGE.id, Update.update(RandomResponses.RESPONSES,
                listOf("Hey there! I noticed you haven’t finished the *{current_standup}* questions, yet. " +
                        "The *{next_standup}* stand-up is about to start. " +
                        "Do you still want to finish up the questions for *{current_standup}*?")))

        val randomResponseProvider = RandomResponseProvider(randomResponsesRepository)

        Assertions.assertEquals(randomResponseProvider.getRandomizedResponse(ResponseType.REMINDER_MESSAGE, MessageContext(currentStandup = "current",
                date = LocalDate.now(), nextStandup = "next", teamId = "team", userId = "user")),
                "Hey there! I noticed you haven’t finished the *current* questions, yet. The *next* stand-up is about to start. Do you still want to finish up the questions for *current*?")
    }
}