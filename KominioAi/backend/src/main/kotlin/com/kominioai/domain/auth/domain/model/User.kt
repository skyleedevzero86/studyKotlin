package com.kominioai.domain.auth.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UserId,
    val email: Email,
    val passwordHash: PasswordHash,
    val username: Username,
    val firstName: String?,
    val lastName: String?,
    val profileImageUrl: String?,
    val phone: String?,
    val accountStatus: AccountStatus,
    val emailVerified: Boolean,
    val emailVerificationToken: String?,
    val emailVerificationExpiresAt: LocalDateTime?,
    val passwordResetToken: String?,
    val passwordResetExpiresAt: LocalDateTime?,
    val lastLoginAt: LocalDateTime?,
    val failedLoginAttempts: Int,
    val accountLockedUntil: LocalDateTime?,
    val twoFactorEnabled: Boolean,
    val twoFactorSecret: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun isAccountLocked(): Boolean {
        return accountLockedUntil?.isAfter(LocalDateTime.now()) == true
    }

    fun isEmailVerified(): Boolean = emailVerified

    fun incrementFailedLoginAttempts(): User {
        return copy(
            failedLoginAttempts = failedLoginAttempts + 1,
            updatedAt = LocalDateTime.now()
        )
    }

    fun resetFailedLoginAttempts(): User {
        return copy(
            failedLoginAttempts = 0,
            accountLockedUntil = null,
            updatedAt = LocalDateTime.now()
        )
    }

    fun lockAccount(lockDurationMinutes: Int): User {
        return copy(
            accountLockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes.toLong()),
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateLastLogin(): User {
        return copy(
            lastLoginAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    fun enableTwoFactor(secret: String): User {
        return copy(
            twoFactorEnabled = true,
            twoFactorSecret = secret,
            updatedAt = LocalDateTime.now()
        )
    }

    fun disableTwoFactor(): User {
        return copy(
            twoFactorEnabled = false,
            twoFactorSecret = null,
            updatedAt = LocalDateTime.now()
        )
    }

    fun verifyEmail(): User {
        return copy(
            emailVerified = true,
            emailVerificationToken = null,
            emailVerificationExpiresAt = null,
            updatedAt = LocalDateTime.now()
        )
    }

    fun setEmailVerificationToken(token: String, expiresAt: LocalDateTime): User {
        return copy(
            emailVerificationToken = token,
            emailVerificationExpiresAt = expiresAt,
            updatedAt = LocalDateTime.now()
        )
    }

    fun setPasswordResetToken(token: String, expiresAt: LocalDateTime): User {
        return copy(
            passwordResetToken = token,
            passwordResetExpiresAt = expiresAt,
            updatedAt = LocalDateTime.now()
        )
    }

    fun clearPasswordResetToken(): User {
        return copy(
            passwordResetToken = null,
            passwordResetExpiresAt = null,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updatePassword(newPasswordHash: PasswordHash): User {
        return copy(
            passwordHash = newPasswordHash,
            passwordResetToken = null,
            passwordResetExpiresAt = null,
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateProfile(
        firstName: String?,
        lastName: String?,
        profileImageUrl: String?,
        phone: String?
    ): User {
        return copy(
            firstName = firstName,
            lastName = lastName,
            profileImageUrl = profileImageUrl,
            phone = phone,
            updatedAt = LocalDateTime.now()
        )
    }

    fun activateAccount(): User {
        return copy(
            accountStatus = AccountStatus.ACTIVE,
            updatedAt = LocalDateTime.now()
        )
    }

    fun deactivateAccount(): User {
        return copy(
            accountStatus = AccountStatus.INACTIVE,
            updatedAt = LocalDateTime.now()
        )
    }

    fun suspendAccount(): User {
        return copy(
            accountStatus = AccountStatus.SUSPENDED,
            updatedAt = LocalDateTime.now()
        )
    }

    companion object {
        fun create(
            email: Email,
            passwordHash: PasswordHash,
            username: Username,
            firstName: String? = null,
            lastName: String? = null,
            phone: String? = null
        ): User {
            val now = LocalDateTime.now()
            return User(
                id = UserId(UUID.randomUUID().toString()),
                email = email,
                passwordHash = passwordHash,
                username = username,
                firstName = firstName,
                lastName = lastName,
                profileImageUrl = null,
                phone = phone,
                accountStatus = AccountStatus.ACTIVE,
                emailVerified = false,
                emailVerificationToken = null,
                emailVerificationExpiresAt = null,
                passwordResetToken = null,
                passwordResetExpiresAt = null,
                lastLoginAt = null,
                failedLoginAttempts = 0,
                accountLockedUntil = null,
                twoFactorEnabled = false,
                twoFactorSecret = null,
                createdAt = now,
                updatedAt = now
            )
        }

        fun reconstruct(
            id: String,
            email: String,
            passwordHash: String,
            username: String,
            firstName: String?,
            lastName: String?,
            profileImageUrl: String?,
            phone: String?,
            accountStatus: String,
            emailVerified: Boolean,
            emailVerificationToken: String?,
            emailVerificationExpiresAt: LocalDateTime?,
            passwordResetToken: String?,
            passwordResetExpiresAt: LocalDateTime?,
            lastLoginAt: LocalDateTime?,
            failedLoginAttempts: Int,
            accountLockedUntil: LocalDateTime?,
            twoFactorEnabled: Boolean,
            twoFactorSecret: String?,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): User {
            return User(
                id = UserId(id),
                email = Email(email),
                passwordHash = PasswordHash(passwordHash),
                username = Username(username),
                firstName = firstName,
                lastName = lastName,
                profileImageUrl = profileImageUrl,
                phone = phone,
                accountStatus = AccountStatus.valueOf(accountStatus),
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
}