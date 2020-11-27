package com.kreait.bots.agile.domain.common.data

object Command {

        const val CREATE = "create"
        const val EDIT = "edit"
        const val DELETE = "delete"
        const val JOIN = "join"
        const val LEAVE = "leave"
        const val TRIGGER = "trigger"
        const val SKIP = "skip"
        const val LIST = "list"
        const val HELP = "help"

        val ALL = listOf(CREATE, EDIT, DELETE, JOIN, LEAVE, TRIGGER, SKIP, LIST, HELP)
}