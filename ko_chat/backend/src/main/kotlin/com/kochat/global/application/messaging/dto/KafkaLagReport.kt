package com.kochat.global.application.messaging.dto

import java.time.LocalDateTime

data class KafkaLagReport(
    val snapshots: List<ConsumerLagSnapshot>,
    val healthy: Boolean,
    val maxLag: Long,
    val checkedAt: LocalDateTime,
)
