package com.kochat.global.application.admin

import com.kochat.domain.chat.model.ChatRoomType
import com.kochat.domain.chat.model.MessageType
import java.time.LocalDate
import java.time.LocalDateTime

data class StatisticsFilter(
    val from: LocalDate,
    val to: LocalDate,
    val roomType: ChatRoomType? = null,
    val messageType: MessageType? = null,
) {
    val fromDateTime: LocalDateTime = from.atStartOfDay()
    val toDateTimeExclusive: LocalDateTime = to.plusDays(1).atStartOfDay()
}
