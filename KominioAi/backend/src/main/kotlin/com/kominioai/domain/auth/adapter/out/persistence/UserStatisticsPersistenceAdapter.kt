package com.kominioai.domain.auth.adapter.out.persistence

import com.kominioai.domain.auth.domain.repository.UserStatisticsRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class UserStatisticsPersistenceAdapter(
    private val client: DatabaseClient
) : UserStatisticsRepository {

    override fun countTotalUsers(): Mono<Long> =
        client.sql("SELECT COUNT(*) FROM users")
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }

    override fun countActiveUsers(): Mono<Long> =
        client.sql("SELECT COUNT(*) FROM users WHERE account_status = 'ACTIVE'")
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }

    override fun countSuspendedUsers(): Mono<Long> =
        client.sql("SELECT COUNT(*) FROM users WHERE account_status = 'SUSPENDED'")
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }

    override fun countAdminUsers(): Mono<Long> =
        client.sql("SELECT COUNT(*) FROM user_roles WHERE role_name = 'ADMIN' AND active = true")
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }

    override fun countUsersRegisteredToday(): Mono<Long> {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return client.sql("SELECT COUNT(*) FROM users WHERE DATE(created_at) = :today")
            .bind("today", today)
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }
    }

    override fun countUsersWith2FAEnabled(): Mono<Long> =
        client.sql("SELECT COUNT(*) FROM users WHERE two_factor_enabled = true")
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }
}