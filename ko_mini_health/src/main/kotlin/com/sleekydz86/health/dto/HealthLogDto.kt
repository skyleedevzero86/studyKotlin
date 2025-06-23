package com.sleekydz86.health.dto

import java.time.LocalDateTime

data class HealthLogDto(
    var logId: Long = 0,
    var userId: String = "",
    var sleepHours: Float = 0f,
    var steps: Int = 0,
    var stressLevel: Float = 0f,
    var heartRate: Float = 0f,
    var logDate: LocalDateTime = LocalDateTime.now(),
    var warning: Boolean = false,
    var memo: String? = null
)