package com.kreait.bots.agile.domain.response

enum class ResponseType(val description: String, val id: String, val fallback: String) {
    UNKNOWN_MESSAGE_RESPONSE("When Olaph does not understand", "UnknownMessageResponses", "I couldn't understand that, maybe try it in owlish?"),
    HOOT_RESPONSES("Easter egg message", "HootResponses", "Hoot hoot! Did you know, that owls can turn their heads as much as 270 degrees? That's why I don't need a swivel chair."),
    BROADCAST_CONFIRMATION("Confirmation message after all questions been answered", "BroadcastConfirmations", "Thank you for that! How about some cheese? :cheese_wedge: "),
    OPENING("Opening message for a standup", "RandomOpeningMessages", "Hoot hoot, there is a new stand-up. Here are your questions for *{current_standup}*."),
    DELETE_SUCCESS("After succesfully deleting a standup", "DeleteSuccessMessage", "Successfully deleted stand-up *{current_standup}*"),
    LEAVE_CONFIRMATION("After leaving a standup with the leave command", "LeaveConfirmationMessage", "You left the standup *{current_standup}*"),
    SUCCESFUL_SKIPPED("After skipping a standup", "SuccesfulSkippedMessage", "You skipped the standup *{current_standup}* today."),
    NO_STANDUPS_FOUND("After not finding a standup/via command", "NoStandupsFound", "Oops, there are no standups."),
    CREATION_SUCCESS("After successfully creating a standup", "CreationSuccess", "Stand-up {current_standup} defined :thumbsup_all:"),
    EDIT_INTRO("Edit Intro Message", "EditCommandIntro", "Hoot Hoot, seems like you want to edit a stand-up."),
    SELECT_STANDUP_TO_EDIT("Standup selection text", "SelectStandupDefinitionToEdit", "Please select the stand-up you want to edit"),
    EDIT_SUCCESS("After Successfully editing a standup", "EditSuccess", "Successfully edited stand-up *{current_standup}*."),
    SUCCESS_JOIN("After joining a standup", "SuccessJoin", "You joined stand-up *{current_standup}*"),
    EXPIRATION_MESSAGE("After standup expires", "ExpirationMessage", "The stand-up *{current_standup}* has been expired. No cookie for you today."),
    REMINDER_MESSAGE("Reminder message", "ReminderMessageText", "Hey there! I noticed you haven’t finished the *{current_standup}* questions, yet. The *{next_standup}* stand-up is about to start. Do you still want to finish up the questions for *{current_standup}*?"),
    CHANNEL_JOIN("After channel joining", "ChannelStandupInform", "Hoot Hoot! By joining this channel you also joined the following stand-ups:")
}

