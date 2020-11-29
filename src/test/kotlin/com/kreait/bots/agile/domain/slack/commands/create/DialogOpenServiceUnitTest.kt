package com.kreait.bots.agile.domain.slack.commands.create

import com.kreait.bots.agile.UnitTest
import com.kreait.bots.agile.domain.slack.standupDefinition.create.dialog.open.CreateDialogOpeningService
import com.kreait.slack.api.test.MockSlackClient
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Locale

@UnitTest
class DialogOpenServiceUnitTest {

    @DisplayName("Test CreateDialogOpeningService")
    @Test
    fun test() {
        val mockSlackClient = MockSlackClient()

        val service = CreateDialogOpeningService(slackClient = mockSlackClient,
            dialogOptionService = mock { },
            message = mock {
                on { getMessage(any<String>(), any<Locale>()) } doReturn "token"
            })

        service.openCreationDialog("sampleTrigger", "sampleUser", "sampleTeam")
        //TODO add verifaction or something


    }
}
