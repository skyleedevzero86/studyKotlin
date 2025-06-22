package com.sleekydz86.health.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import java.time.LocalDateTime

@Entity
@Table(name = "health_logs")
data class HealthLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var logId: Long = 0,
    @Column(name = "user_id")
    var userId: String? = null,
    var sleepHours: Float = 0f,
    var steps: Int = 0,
    var stressLevel: Float = 0f,
    var heartRate: Float = 0f,
    var logDate: LocalDateTime = LocalDateTime.now(),
    var warning: Boolean = false
)