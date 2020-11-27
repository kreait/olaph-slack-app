package com.kreait.bots.agile.domain.slack

interface SlackEventTest {

    fun supportsEvent()

    fun onReceiveEvent()
}