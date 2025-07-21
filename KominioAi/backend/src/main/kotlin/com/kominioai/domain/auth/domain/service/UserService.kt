package com.kominioai.domain.auth.domain.service

import com.kominioai.domain.auth.domain.model.User
import com.kominioai.domain.auth.domain.model.AccountStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService {
    fun canUserLogin(user: User): Boolean {
        return user.accountStatus == AccountStatus.ACTIVE &&
                !user.isAccountLocked() &&
                user.emailVerified
    }

    fun shouldLockAccount(failedAttempts: Int, maxAttempts: Int): Boolean {
        return failedAttempts >= maxAttempts
    }

    fun calculateLockDuration(failedAttempts: Int): Int {
        return when {
            failedAttempts <= 3 -> 5
            failedAttempts <= 5 -> 15
            failedAttempts <= 10 -> 30
            else -> 60
        }
    }

    fun isSuspiciousActivity(
        user: User,
        ipAddress: String?,
        userAgent: String?,
        lastLoginAt: LocalDateTime?
    ): Boolean {
        if (lastLoginAt == null) return false

        val timeDiff = java.time.Duration.between(lastLoginAt, LocalDateTime.now())
        val isRecentLogin = timeDiff.toMinutes() < 5

        return isRecentLogin && (ipAddress != user.lastLoginAt?.toString() || userAgent != user.lastLoginAt?.toString())
    }

    fun generateDisplayName(firstName: String?, lastName: String?, username: String): String {
        return when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName
            !lastName.isNullOrBlank() -> lastName
            else -> username
        }
    }
}