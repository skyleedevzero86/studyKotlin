package com.sleekydz86.oauth.domain.user.model

import java.time.Instant
import java.time.temporal.ChronoUnit

class User private constructor(
    val id: Long?,
    val username: String,
    val password: String,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: Instant,
    val passwordChangedAt: Instant,
    val passwordChangeFailCount: Int,
    val lastLoginAt: Instant?,
) {
    fun isPasswordExpired(now: Instant = Instant.now()): Boolean =
        ChronoUnit.DAYS.between(passwordChangedAt, now) >= PASSWORD_VALID_DAYS

    fun canLogin(): Boolean = status == UserStatus.ACTIVE

    fun withStatus(status: UserStatus): User = copy(status = status)

    fun withRole(role: UserRole): User = copy(role = role)

    fun withPassword(encodedPassword: String, changedAt: Instant): User =
        copy(
            password = encodedPassword,
            passwordChangedAt = changedAt,
            passwordChangeFailCount = 0,
            status = if (status == UserStatus.PASSWORD_LOCKED) UserStatus.ACTIVE else status,
        )

    fun withPasswordChangeFailCount(count: Int): User = copy(passwordChangeFailCount = count)

    fun withPasswordLocked(): User = copy(status = UserStatus.PASSWORD_LOCKED, passwordChangeFailCount = MAX_PASSWORD_CHANGE_FAILS)

    fun withLastLoginAt(at: Instant): User = copy(lastLoginAt = at)

    private fun copy(
        id: Long? = this.id,
        username: String = this.username,
        password: String = this.password,
        role: UserRole = this.role,
        status: UserStatus = this.status,
        createdAt: Instant = this.createdAt,
        passwordChangedAt: Instant = this.passwordChangedAt,
        passwordChangeFailCount: Int = this.passwordChangeFailCount,
        lastLoginAt: Instant? = this.lastLoginAt,
    ): User = User(
        id, username, password, role, status, createdAt, passwordChangedAt, passwordChangeFailCount, lastLoginAt,
    )

    companion object {
        const val PASSWORD_VALID_DAYS = 30L
        const val MAX_PASSWORD_CHANGE_FAILS = 3

        fun createPending(
            username: String,
            encodedPassword: String,
            role: UserRole = UserRole.USER,
            now: Instant = Instant.now(),
        ): User = User(
            id = null,
            username = username,
            password = encodedPassword,
            role = role,
            status = UserStatus.PENDING,
            createdAt = now,
            passwordChangedAt = now,
            passwordChangeFailCount = 0,
            lastLoginAt = null,
        )

        fun createActiveAdmin(
            username: String,
            encodedPassword: String,
            now: Instant = Instant.now(),
        ): User = User(
            id = null,
            username = username,
            password = encodedPassword,
            role = UserRole.ADMIN,
            status = UserStatus.ACTIVE,
            createdAt = now,
            passwordChangedAt = now,
            passwordChangeFailCount = 0,
            lastLoginAt = null,
        )

        fun restore(
            id: Long?,
            username: String,
            password: String,
            role: UserRole,
            status: UserStatus,
            createdAt: Instant,
            passwordChangedAt: Instant,
            passwordChangeFailCount: Int,
            lastLoginAt: Instant?,
        ): User = User(id, username, password, role, status, createdAt, passwordChangedAt, passwordChangeFailCount, lastLoginAt)
    }
}
