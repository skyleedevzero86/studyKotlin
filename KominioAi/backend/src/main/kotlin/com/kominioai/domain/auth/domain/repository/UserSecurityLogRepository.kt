package com.kominioai.domain.auth.domain.repository

import com.kominioai.domain.auth.domain.model.UserSecurityLog
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserSecurityLogRepository {
    fun findByUserId(userId: String, page: Int, size: Int): Flux<UserSecurityLog>
    fun countByUserId(userId: String): Mono<Long>
    fun save(securityLog: UserSecurityLog): Mono<UserSecurityLog>
    fun findByUserIdAndEventType(userId: String, eventType: String): Flux<UserSecurityLog>
    fun findRecentSecurityLogs(userId: String, limit: Int): Flux<UserSecurityLog>
    fun countFailedLoginAttempts(userId: String, since: java.time.LocalDateTime): Mono<Long>
    fun findSuspiciousActivities(userId: String): Flux<UserSecurityLog>
}