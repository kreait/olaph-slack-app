package com.kreait.bots.agile.domain.slack.standupDefinition

import com.kreait.bots.agile.core.configuration.converter.LocalTimeToStringConverter
import com.kreait.bots.agile.domain.common.service.UserService
import com.kreait.slack.api.contract.jackson.group.dialog.Options
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.util.stream.IntStream
import kotlin.streams.toList

/**
 * TODO this class is heavily bound to edit and create it should not be on this package level
 */
@Service
class DialogOptionService @Autowired constructor(private val userService: UserService) {

    /**
     * Fetches the conversations and prepares them for the Dialog
     * @return list of channel options
     */
    fun createChannelOptions(token: String, userId: String): List<Options> {
        return userService.conversationList(token, userId).map { channel ->
            if (channel.isPrivate) {
                Options(":lock: ${channel.nameNormalized}", channel.id)
            } else {
                Options(":hash: ${channel.nameNormalized}", channel.id)
            }
        }
    }

    companion object {
        /**
         * Creates Time Options
         * @return list of time options
         */
        fun createTimeOptions(): List<Options> {
            return IntStream.rangeClosed(0, 23) // create integer stream from 0 to 23
                    .mapToObj { it } // transform int stream to object stream
                    .flatMap { hour ->
                        // for each hour create 4 LocalTime objects
                        IntStream.of(0, 15, 30, 45)
                                .mapToObj { quarter -> LocalTime.of(hour, quarter) }
                    }
                    .map {
                        // map to options object
                        val time = LocalTimeToStringConverter.INSTANCE.convert(it)
                        Options(time, time)
                    }
                    .toList()
        }
    }


}
