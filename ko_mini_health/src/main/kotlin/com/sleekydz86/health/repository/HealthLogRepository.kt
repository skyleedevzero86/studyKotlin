package com.sleekydz86.health.repository

import com.sleekydz86.health.entity.HealthLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface HealthLogRepository : JpaRepository<HealthLog, Long> {
    @Query("SELECT h FROM HealthLog h WHERE h.userId = :userId AND h.logDate BETWEEN :startDate AND :endDate")
    fun findByUserIdAndDateRange(userId: String, startDate: LocalDateTime, endDate: LocalDateTime): List<HealthLog>
}