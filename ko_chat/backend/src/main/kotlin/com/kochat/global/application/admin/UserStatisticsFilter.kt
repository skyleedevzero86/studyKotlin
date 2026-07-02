package com.kochat.global.application.admin

import com.kochat.domain.user.model.UserActivityEventType
import java.time.LocalDate
import java.time.LocalDateTime

data class UserStatisticsFilter(
    val from: LocalDate,
    val to: LocalDate,
    val eventType: UserActivityEventType? = null,
) {
    val fromDateTime: LocalDateTime = from.atStartOfDay()
    val toDateTimeExclusive: LocalDateTime = to.plusDays(1).atStartOfDay()
}
