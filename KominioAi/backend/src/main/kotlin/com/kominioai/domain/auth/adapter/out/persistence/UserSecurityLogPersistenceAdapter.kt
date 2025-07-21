package com.kominioai.domain.auth.adapter.out.persistence

import com.kominioai.domain.auth.domain.model.UserSecurityLog
import com.kominioai.domain.auth.domain.repository.UserSecurityLogRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class UserSecurityLogPersistenceAdapter(
    private val client: DatabaseClient
) : UserSecurityLogRepository {

    override fun findByUserId(userId: String, page: Int, size: Int): Flux<UserSecurityLog> {
        val offset = (page - 1) * size
        return client.sql(
            """
            SELECT * FROM user_security_logs
            WHERE user_id = :userId
            ORDER BY created_at DESC
            LIMIT :size OFFSET :offset
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("size", size)
            .bind("offset", offset)
            .map { row, _ -> rowToUserSecurityLog(row) }
            .all()
    }

    override fun countByUserId(userId: String): Mono<Long> {
        return client.sql(
            """
            SELECT COUNT(*) FROM user_security_logs WHERE user_id = :userId
            """.trimIndent()
        )
            .bind("userId", userId)
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }
    }

    override fun save(securityLog: UserSecurityLog): Mono<UserSecurityLog> {
        val sql = """
            INSERT INTO user_security_logs (id, user_id, event_type, event_description, ip_address, user_agent, success, failure_reason, created_at)
            VALUES (:id, :userId, :eventType, :eventDescription, :ipAddress, :userAgent, :success, :failureReason, :createdAt)
            ON CONFLICT (id) DO UPDATE SET
                event_type = EXCLUDED.event_type,
                event_description = EXCLUDED.event_description,
                ip_address = EXCLUDED.ip_address,
                user_agent = EXCLUDED.user_agent,
                success = EXCLUDED.success,
                failure_reason = EXCLUDED.failure_reason
        """.trimIndent()

        val bindSpec = client.sql(sql)
            .bind("id", securityLog.id)
            .bind("userId", securityLog.userId)
            .bind("eventType", securityLog.eventType)
            .bind("eventDescription", securityLog.eventDescription)
            .bind("success", securityLog.success)
            .bind("createdAt", securityLog.createdAt)

        val finalBindSpec = bindSpec
            .let { spec ->
                if (securityLog.ipAddress != null) spec.bind("ipAddress", securityLog.ipAddress) else spec.bindNull("ipAddress", String::class.java)
            }
            .let { spec ->
                if (securityLog.userAgent != null) spec.bind("userAgent", securityLog.userAgent) else spec.bindNull("userAgent", String::class.java)
            }
            .let { spec ->
                if (securityLog.failureReason != null) spec.bind("failureReason", securityLog.failureReason) else spec.bindNull("failureReason", String::class.java)
            }

        return finalBindSpec.then().thenReturn(securityLog)
    }

    override fun findByUserIdAndEventType(userId: String, eventType: String): Flux<UserSecurityLog> {
        return client.sql(
            """
            SELECT * FROM user_security_logs
            WHERE user_id = :userId AND event_type = :eventType
            ORDER BY created_at DESC
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("eventType", eventType)
            .map { row, _ -> rowToUserSecurityLog(row) }
            .all()
    }

    override fun findRecentSecurityLogs(userId: String, limit: Int): Flux<UserSecurityLog> {
        return client.sql(
            """
            SELECT * FROM user_security_logs
            WHERE user_id = :userId
            ORDER BY created_at DESC
            LIMIT :limit
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("limit", limit)
            .map { row, _ -> rowToUserSecurityLog(row) }
            .all()
    }

    override fun countFailedLoginAttempts(userId: String, since: LocalDateTime): Mono<Long> {
        return client.sql(
            """
            SELECT COUNT(*) FROM user_security_logs
            WHERE user_id = :userId 
            AND event_type = 'LOGIN_FAILED' 
            AND success = false 
            AND created_at >= :since
            """.trimIndent()
        )
            .bind("userId", userId)
            .bind("since", since)
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }
    }

    override fun findSuspiciousActivities(userId: String): Flux<UserSecurityLog> {
        return client.sql(
            """
            SELECT * FROM user_security_logs
            WHERE user_id = :userId 
            AND (
                event_type IN ('LOGIN_FAILED', 'SUSPICIOUS_ACTIVITY', 'PASSWORD_RESET_ATTEMPT')
                OR success = false
            )
            ORDER BY created_at DESC
            LIMIT 50
            """.trimIndent()
        )
            .bind("userId", userId)
            .map { row, _ -> rowToUserSecurityLog(row) }
            .all()
    }

    private fun rowToUserSecurityLog(row: io.r2dbc.spi.Row): UserSecurityLog {
        val id = row.get("id", String::class.java) ?: ""
        val userId = row.get("user_id", String::class.java) ?: ""
        val eventType = row.get("event_type", String::class.java) ?: ""
        val eventDescription = row.get("event_description", String::class.java) ?: ""
        val ipAddress = row.get("ip_address", String::class.java)
        val userAgent = row.get("user_agent", String::class.java)
        val success = row.get("success", java.lang.Boolean::class.java)?.booleanValue() ?: false
        val failureReason = row.get("failure_reason", String::class.java)
        val createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now()

        return UserSecurityLog(
            id = id,
            userId = userId,
            eventType = eventType,
            eventDescription = eventDescription,
            ipAddress = ipAddress,
            userAgent = userAgent,
            success = success,
            failureReason = failureReason,
            createdAt = createdAt
        )
    }
}