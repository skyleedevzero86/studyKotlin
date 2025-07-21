package com.kominioai.domain.auth.adapter.out.persistence

import com.kominioai.domain.auth.application.port.out.LoadUserPort
import com.kominioai.domain.auth.application.port.out.SaveUserPort
import com.kominioai.domain.auth.domain.model.*
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Component
class UserPersistenceAdapter(
    private val client: DatabaseClient
) : LoadUserPort, SaveUserPort {

    override fun loadById(id: UserId): Mono<User?> =
        client.sql("SELECT * FROM users WHERE id = :id")
            .bind("id", id.value)
            .map { row, _ -> rowToUser(row) }
            .one()

    override fun loadByEmail(email: Email): Mono<User?> =
        client.sql("SELECT * FROM users WHERE email = :email")
            .bind("email", email.value)
            .map { row, _ -> rowToUser(row) }
            .one()

    override fun loadByUsername(username: Username): Mono<User?> =
        client.sql("SELECT * FROM users WHERE username = :username")
            .bind("username", username.value)
            .map { row, _ -> rowToUser(row) }
            .one()

    override fun loadByEmailOrUsername(emailOrUsername: String): Mono<User?> =
        client.sql("SELECT * FROM users WHERE email = :v OR username = :v")
            .bind("v", emailOrUsername)
            .map { row, _ -> rowToUser(row) }
            .one()

    override fun loadAll(page: Int, size: Int): Flux<User> =
        client.sql("SELECT * FROM users ORDER BY created_at DESC LIMIT :size OFFSET :offset")
            .bind("size", size)
            .bind("offset", (page - 1) * size)
            .map { row, _ -> rowToUser(row) }
            .all()

    override fun loadByAccountStatus(status: String, page: Int, size: Int): Flux<User> =
        client.sql("SELECT * FROM users WHERE account_status = :status ORDER BY created_at DESC LIMIT :size OFFSET :offset")
            .bind("status", status)
            .bind("size", size)
            .bind("offset", (page - 1) * size)
            .map { row, _ -> rowToUser(row) }
            .all()

    override fun searchUsers(query: String, page: Int, size: Int): Flux<User> =
        client.sql("SELECT * FROM users WHERE email ILIKE :q OR username ILIKE :q ORDER BY created_at DESC LIMIT :size OFFSET :offset")
            .bind("q", "%$query%")
            .bind("size", size)
            .bind("offset", (page - 1) * size)
            .map { row, _ -> rowToUser(row) }
            .all()

    override fun existsByEmail(email: Email): Mono<Boolean> =
        client.sql("SELECT COUNT(*) FROM users WHERE email = :email")
            .bind("email", email.value)
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it > 0 }

    override fun existsByUsername(username: Username): Mono<Boolean> =
        client.sql("SELECT COUNT(*) FROM users WHERE username = :username")
            .bind("username", username.value)
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it > 0 }

    override fun countByAccountStatus(status: String): Mono<Long> =
        client.sql("SELECT COUNT(*) FROM users WHERE account_status = :status")
            .bind("status", status)
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }

    override fun countTotalUsers(): Mono<Long> =
        client.sql("SELECT COUNT(*) FROM users")
            .map { row, _ -> row.get(0, java.lang.Long::class.java) ?: 0L }
            .one()
            .map { it.toLong() }

    override fun save(user: User): Mono<User> {
        val sql = """
            INSERT INTO users (id, email, password_hash, username, first_name, last_name, profile_image_url, phone, account_status, email_verified, email_verification_token, email_verification_expires_at, password_reset_token, password_reset_expires_at, last_login_at, failed_login_attempts, account_locked_until, two_factor_enabled, two_factor_secret, created_at, updated_at)
            VALUES (:id, :email, :password_hash, :username, :first_name, :last_name, :profile_image_url, :phone, :account_status, :email_verified, :email_verification_token, :email_verification_expires_at, :password_reset_token, :password_reset_expires_at, :last_login_at, :failed_login_attempts, :account_locked_until, :two_factor_enabled, :two_factor_secret, :created_at, :updated_at)
            ON CONFLICT (id) DO UPDATE SET
                email = EXCLUDED.email,
                password_hash = EXCLUDED.password_hash,
                username = EXCLUDED.username,
                first_name = EXCLUDED.first_name,
                last_name = EXCLUDED.last_name,
                profile_image_url = EXCLUDED.profile_image_url,
                phone = EXCLUDED.phone,
                account_status = EXCLUDED.account_status,
                email_verified = EXCLUDED.email_verified,
                email_verification_token = EXCLUDED.email_verification_token,
                email_verification_expires_at = EXCLUDED.email_verification_expires_at,
                password_reset_token = EXCLUDED.password_reset_token,
                password_reset_expires_at = EXCLUDED.password_reset_expires_at,
                last_login_at = EXCLUDED.last_login_at,
                failed_login_attempts = EXCLUDED.failed_login_attempts,
                account_locked_until = EXCLUDED.account_locked_until,
                two_factor_enabled = EXCLUDED.two_factor_enabled,
                two_factor_secret = EXCLUDED.two_factor_secret,
                updated_at = EXCLUDED.updated_at
        """.trimIndent()

        val bindSpec = client.sql(sql)
            .bind("id", user.id.value)
            .bind("email", user.email.value)
            .bind("password_hash", user.passwordHash.value)
            .bind("username", user.username.value)
            .bind("account_status", user.accountStatus.name)
            .bind("email_verified", user.emailVerified)
            .bind("failed_login_attempts", user.failedLoginAttempts)
            .bind("two_factor_enabled", user.twoFactorEnabled)
            .bind("created_at", user.createdAt)
            .bind("updated_at", user.updatedAt)

        val finalBindSpec = bindSpec
            .let { spec ->
                if (user.firstName != null) spec.bind("first_name", user.firstName) else spec.bindNull("first_name", String::class.java)
            }
            .let { spec ->
                if (user.lastName != null) spec.bind("last_name", user.lastName) else spec.bindNull("last_name", String::class.java)
            }
            .let { spec ->
                if (user.profileImageUrl != null) spec.bind("profile_image_url", user.profileImageUrl) else spec.bindNull("profile_image_url", String::class.java)
            }
            .let { spec ->
                if (user.phone != null) spec.bind("phone", user.phone) else spec.bindNull("phone", String::class.java)
            }
            .let { spec ->
                if (user.emailVerificationToken != null) spec.bind("email_verification_token", user.emailVerificationToken) else spec.bindNull("email_verification_token", String::class.java)
            }
            .let { spec ->
                if (user.emailVerificationExpiresAt != null) spec.bind("email_verification_expires_at", user.emailVerificationExpiresAt) else spec.bindNull("email_verification_expires_at", LocalDateTime::class.java)
            }
            .let { spec ->
                if (user.passwordResetToken != null) spec.bind("password_reset_token", user.passwordResetToken) else spec.bindNull("password_reset_token", String::class.java)
            }
            .let { spec ->
                if (user.passwordResetExpiresAt != null) spec.bind("password_reset_expires_at", user.passwordResetExpiresAt) else spec.bindNull("password_reset_expires_at", LocalDateTime::class.java)
            }
            .let { spec ->
                if (user.lastLoginAt != null) spec.bind("last_login_at", user.lastLoginAt) else spec.bindNull("last_login_at", LocalDateTime::class.java)
            }
            .let { spec ->
                if (user.accountLockedUntil != null) spec.bind("account_locked_until", user.accountLockedUntil) else spec.bindNull("account_locked_until", LocalDateTime::class.java)
            }
            .let { spec ->
                if (user.twoFactorSecret != null) spec.bind("two_factor_secret", user.twoFactorSecret) else spec.bindNull("two_factor_secret", String::class.java)
            }

        return finalBindSpec.then().thenReturn(user)
    }

    override fun deleteById(id: String): Mono<Boolean> =
        client.sql("DELETE FROM users WHERE id = :id")
            .bind("id", id)
            .fetch().rowsUpdated()
            .map { it > 0 }

    private fun rowToUser(row: io.r2dbc.spi.Row): User {
        val id = row.get("id", String::class.java) ?: ""
        val email = row.get("email", String::class.java) ?: ""
        val passwordHash = row.get("password_hash", String::class.java) ?: ""
        val username = row.get("username", String::class.java) ?: ""
        val firstName = row.get("first_name", String::class.java)
        val lastName = row.get("last_name", String::class.java)
        val profileImageUrl = row.get("profile_image_url", String::class.java)
        val phone = row.get("phone", String::class.java)
        val accountStatus = row.get("account_status", String::class.java) ?: "ACTIVE"
        val emailVerified = row.get("email_verified", java.lang.Boolean::class.java)?.booleanValue() ?: false
        val emailVerificationToken = row.get("email_verification_token", String::class.java)
        val emailVerificationExpiresAt = row.get("email_verification_expires_at", LocalDateTime::class.java)
        val passwordResetToken = row.get("password_reset_token", String::class.java)
        val passwordResetExpiresAt = row.get("password_reset_expires_at", LocalDateTime::class.java)
        val lastLoginAt = row.get("last_login_at", LocalDateTime::class.java)
        val failedLoginAttempts = row.get("failed_login_attempts", Integer::class.java)?.toInt() ?: 0
        val accountLockedUntil = row.get("account_locked_until", LocalDateTime::class.java)
        val twoFactorEnabled = row.get("two_factor_enabled", java.lang.Boolean::class.java)?.booleanValue() ?: false
        val twoFactorSecret = row.get("two_factor_secret", String::class.java)
        val createdAt = row.get("created_at", LocalDateTime::class.java) ?: LocalDateTime.now()
        val updatedAt = row.get("updated_at", LocalDateTime::class.java) ?: LocalDateTime.now()

        return User.reconstruct(
            id = id,
            email = email,
            passwordHash = passwordHash,
            username = username,
            firstName = firstName,
            lastName = lastName,
            profileImageUrl = profileImageUrl,
            phone = phone,
            accountStatus = accountStatus,
            emailVerified = emailVerified,
            emailVerificationToken = emailVerificationToken,
            emailVerificationExpiresAt = emailVerificationExpiresAt,
            passwordResetToken = passwordResetToken,
            passwordResetExpiresAt = passwordResetExpiresAt,
            lastLoginAt = lastLoginAt,
            failedLoginAttempts = failedLoginAttempts,
            accountLockedUntil = accountLockedUntil,
            twoFactorEnabled = twoFactorEnabled,
            twoFactorSecret = twoFactorSecret,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}