package com.kominioai.domain.auth.application.service

import com.kominioai.domain.auth.application.dto.SecurityLogEntry
import com.kominioai.domain.auth.domain.model.UserSecurityLog

object UserSecurityLogMapper {
    fun toDto(log: UserSecurityLog): SecurityLogEntry =
        SecurityLogEntry(
            id = log.id,
            eventType = log.eventType,
            eventDescription = log.eventDescription,
            ipAddress = log.ipAddress,
            userAgent = log.userAgent,
            success = log.success,
            failureReason = log.failureReason,
            createdAt = log.createdAt
        )
}