package com.kreait.bots.agile.domain.slack

interface InteractiveComponentReceiverTest {

    fun supportsInteractiveMessage()

    fun onReceiveInteractiveMessage()
}
