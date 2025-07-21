package com.kominioai.domain.auth.adapter.out.persistence

import com.kominioai.domain.auth.application.port.out.LoadUserRolePort
import com.kominioai.domain.auth.application.port.out.SaveUserRolePort
import com.kominioai.domain.auth.domain.model.*
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Component
class UserRolePersistenceAdapter(
    private val client: DatabaseClient
) : LoadUserRolePort, SaveUserRolePort {

    override fun loadByUserId(userId: UserId): Flux<UserRole> =
        client.sql("SELECT * FROM user_roles WHERE user_id = :userId")
            .bind("userId", userId.value)
            .map { row, _ -> rowToUserRole(row) }
            .all()

    override fun loadByUserIdAndRoleName(userId: UserId, roleName: String): Mono<UserRole?> =
        client.sql("SELECT * FROM user_roles WHERE user_id = :userId AND role_name = :roleName")
            .bind("userId", userId.value)
            .bind("roleName", roleName)
            .map { row, _ -> rowToUserRole(row) }
            .one()

    override fun loadActiveRolesByUserId(userId: UserId): Flux<UserRole> =
        client.sql("SELECT * FROM user_roles WHERE user_id = :userId AND active = true")
            .bind("userId", userId.value)
            .map { row, _ -> rowToUserRole(row) }
            .all()

    override fun save(role: UserRole): Mono<UserRole> {
        val sql = """
            INSERT INTO user_roles (id, user_id, role_name, granted_at, granted_by, active)
            VALUES (:id, :userId, :roleName, :grantedAt, :grantedBy, :active)
            ON CONFLICT (user_id, role_name) DO UPDATE SET
                granted_at = EXCLUDED.granted_at,
                granted_by = EXCLUDED.granted_by,
                active = EXCLUDED.active
        """.trimIndent()

        val bindSpec = client.sql(sql)
            .bind("id", role.id.value)
            .bind("userId", role.userId.value)
            .bind("roleName", role.roleName)
            .bind("grantedAt", role.grantedAt)
            .bind("active", role.active)

        val finalBindSpec = if (role.grantedBy != null) {
            bindSpec.bind("grantedBy", role.grantedBy!!.value)
        } else {
            bindSpec.bindNull("grantedBy", String::class.java)
        }

        return finalBindSpec.then().thenReturn(role)
    }

    override fun deleteByUserIdAndRoleName(userId: UserId, roleName: String): Mono<Boolean> =
        client.sql("DELETE FROM user_roles WHERE user_id = :userId AND role_name = :roleName")
            .bind("userId", userId.value)
            .bind("roleName", roleName)
            .fetch().rowsUpdated()
            .map { it > 0 }

    override fun deleteByUserId(userId: UserId): Mono<Boolean> =
        client.sql("DELETE FROM user_roles WHERE user_id = :userId")
            .bind("userId", userId.value)
            .fetch().rowsUpdated()
            .map { it > 0 }

    private fun rowToUserRole(row: io.r2dbc.spi.Row): UserRole {
        val id = row.get("id", String::class.java) ?: ""
        val userId = row.get("user_id", String::class.java) ?: ""
        val roleName = row.get("role_name", String::class.java) ?: ""
        val grantedAt = row.get("granted_at", LocalDateTime::class.java) ?: LocalDateTime.now()
        val grantedBy = row.get("granted_by", String::class.java)

        val activeValue = row.get("active", java.lang.Boolean::class.java)
        val active = activeValue?.booleanValue() ?: true

        return UserRole.reconstruct(
            id = id,
            userId = userId,
            roleName = roleName,
            grantedAt = grantedAt,
            grantedBy = grantedBy,
            active = active
        )
    }
}