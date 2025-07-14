package com.kominioai.domain.survey.domain.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class SurveyPeriod(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
) {
    init {
        require(!startDate.isAfter(endDate)) { "시작일은 종료일보다 이전이어야 합니다." }
        require(startDate.isAfter(LocalDateTime.now().minusDays(1))) { "시작일은 과거일 수 없습니다." }
        require(endDate.isAfter(LocalDateTime.now())) { "종료일은 현재보다 이후여야 합니다." }
    }

    fun isActive(now: LocalDateTime = LocalDateTime.now()): Boolean =
        (startDate.isBefore(now) || startDate.isEqual(now)) &&
        (endDate.isAfter(now) || endDate.isEqual(now))

    fun isWaiting(now: LocalDateTime = LocalDateTime.now()): Boolean =
        startDate.isAfter(now)

    fun isCompleted(now: LocalDateTime = LocalDateTime.now()): Boolean =
        endDate.isBefore(now)

    fun getDurationInDays(): Long = ChronoUnit.DAYS.between(startDate, endDate)

    fun getRemainingDays(now: LocalDateTime = LocalDateTime.now()): Long =
        ChronoUnit.DAYS.between(now, endDate)

    fun getProgressPercentage(now: LocalDateTime = LocalDateTime.now()): Double {
        val totalDuration = ChronoUnit.SECONDS.between(startDate, endDate)
        val elapsed = ChronoUnit.SECONDS.between(startDate, now)
        return (elapsed.toDouble() / totalDuration * 100).coerceIn(0.0, 100.0)
    }

    fun display(): String = "${startDate.toLocalDate()} ~ ${endDate.toLocalDate()}"
}