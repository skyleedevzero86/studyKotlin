package com.sleekydz86.oauth.domain.user.model

import java.time.Instant
import java.time.temporal.ChronoUnit

class User private constructor(
    val id: Long?,
    val username: String,
    val password: String,
    val displayName: String?,
    val role: UserRole,
    val status: UserStatus,
    val createdAt: Instant,
    val passwordChangedAt: Instant,
    val passwordChangeFailCount: Int,
    val loginFailCount: Int,
    val lastLoginAt: Instant?,
) {
    fun isPasswordExpired(now: Instant = Instant.now()): Boolean =
        ChronoUnit.DAYS.between(passwordChangedAt, now) >= PASSWORD_VALID_DAYS

    fun canLogin(): Boolean = status == UserStatus.ACTIVE

    fun withStatus(status: UserStatus): User = copy(status = status)

    fun withRole(role: UserRole): User = copy(role = role)

    fun withDisplayName(displayName: String?): User = copy(displayName = displayName)

    fun withPassword(encodedPassword: String, changedAt: Instant): User =
        copy(
            password = encodedPassword,
            passwordChangedAt = changedAt,
            passwordChangeFailCount = 0,
            status = if (status == UserStatus.PASSWORD_LOCKED) UserStatus.ACTIVE else status,
        )

    fun withPasswordChangeFailCount(count: Int): User = copy(passwordChangeFailCount = count)

    fun withLoginFailCount(count: Int): User = copy(loginFailCount = count)

    fun withPasswordLocked(): User =
        copy(status = UserStatus.PASSWORD_LOCKED, passwordChangeFailCount = MAX_PASSWORD_CHANGE_FAILS)

    fun withLastLoginAt(at: Instant): User = copy(lastLoginAt = at, loginFailCount = 0)

    private fun copy(
        id: Long? = this.id,
        username: String = this.username,
        password: String = this.password,
        displayName: String? = this.displayName,
        role: UserRole = this.role,
        status: UserStatus = this.status,
        createdAt: Instant = this.createdAt,
        passwordChangedAt: Instant = this.passwordChangedAt,
        passwordChangeFailCount: Int = this.passwordChangeFailCount,
        loginFailCount: Int = this.loginFailCount,
        lastLoginAt: Instant? = this.lastLoginAt,
    ): User = User(
        id,
        username,
        password,
        displayName,
        role,
        status,
        createdAt,
        passwordChangedAt,
        passwordChangeFailCount,
        loginFailCount,
        lastLoginAt,
    )

    companion object {
        const val PASSWORD_VALID_DAYS = 30L
        const val MAX_PASSWORD_CHANGE_FAILS = 3

        fun createPending(
            username: String,
            encodedPassword: String,
            role: UserRole = UserRole.USER,
            displayName: String? = null,
            now: Instant = Instant.now(),
        ): User = User(
            id = null,
            username = username,
            password = encodedPassword,
            displayName = displayName,
            role = role,
            status = UserStatus.PENDING,
            createdAt = now,
            passwordChangedAt = now,
            passwordChangeFailCount = 0,
            loginFailCount = 0,
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
            displayName = null,
            role = UserRole.ADMIN,
            status = UserStatus.ACTIVE,
            createdAt = now,
            passwordChangedAt = now,
            passwordChangeFailCount = 0,
            loginFailCount = 0,
            lastLoginAt = null,
        )

        fun createByAdmin(
            username: String,
            encodedPassword: String,
            role: UserRole,
            displayName: String?,
            activateImmediately: Boolean,
            now: Instant = Instant.now(),
        ): User = User(
            id = null,
            username = username,
            password = encodedPassword,
            displayName = displayName,
            role = role,
            status = if (activateImmediately) UserStatus.ACTIVE else UserStatus.PENDING,
            createdAt = now,
            passwordChangedAt = now,
            passwordChangeFailCount = 0,
            loginFailCount = 0,
            lastLoginAt = null,
        )

        fun restore(
            id: Long?,
            username: String,
            password: String,
            displayName: String?,
            role: UserRole,
            status: UserStatus,
            createdAt: Instant,
            passwordChangedAt: Instant,
            passwordChangeFailCount: Int,
            loginFailCount: Int,
            lastLoginAt: Instant?,
        ): User = User(
            id,
            username,
            password,
            displayName,
            role,
            status,
            createdAt,
            passwordChangedAt,
            passwordChangeFailCount,
            loginFailCount,
            lastLoginAt,
        )
    }
}
