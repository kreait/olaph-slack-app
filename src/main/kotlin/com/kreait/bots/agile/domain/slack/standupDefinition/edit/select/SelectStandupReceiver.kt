package com.kreait.bots.agile.domain.slack.standupDefinition.edit.select

import com.kreait.bots.agile.domain.slack.standupDefinition.edit.Callback
import com.kreait.bots.agile.domain.slack.standupDefinition.edit.dialog.EditDialogOpeningService
import com.kreait.slack.api.contract.jackson.InteractiveMessage
import com.kreait.slack.broker.receiver.InteractiveComponentReceiver
import com.kreait.slack.broker.store.team.Team
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service

@Service
class SelectStandupReceiver @Autowired constructor(private val editDialogOpeningService: EditDialogOpeningService) : InteractiveComponentReceiver<InteractiveMessage> {
    companion object {
        private val Log = LoggerFactory.getLogger(SelectStandupReceiver::class.java)
    }

    override fun supportsInteractiveMessage(interactiveComponentResponse: InteractiveMessage): Boolean =
            interactiveComponentResponse.callbackId?.equals(Callback.EDIT_STANDUP_SELECTED.id) ?: false


    override fun onReceiveInteractiveMessage(interactiveComponentResponse: InteractiveMessage, headers: HttpHeaders, team: Team) {
        interactiveComponentResponse.team.let {
            editDialogOpeningService.openEditDialog(interactiveComponentResponse.triggerId!!,
                    interactiveComponentResponse.user.id,
                    it.id,
                    interactiveComponentResponse.actions!!.first().selectedOptions!!.first().value, team.bot.accessToken)
        }
    }
}
