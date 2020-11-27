package com.kreait.bots.agile.domain.slack

interface SlashCommandTest {

    fun supportsCommand()

    fun onReceiveSlashCommand()
}
