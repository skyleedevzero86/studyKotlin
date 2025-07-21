package com.kominioai.domain.auth.domain.service

import com.kominioai.domain.auth.domain.model.User
import com.kominioai.domain.auth.domain.model.Email
import com.kominioai.domain.auth.domain.model.Username
import com.kominioai.domain.auth.domain.model.PasswordHash
import com.kominioai.domain.auth.domain.model.AccountStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(
    private val passwordEncoder: PasswordEncoder
) {
    fun validateCredentials(user: User, rawPassword: String): Boolean {
        if (user.accountStatus != AccountStatus.ACTIVE) {
            return false
        }

        if (user.isAccountLocked()) {
            return false
        }

        return passwordEncoder.matches(rawPassword, user.passwordHash.value)
    }

    fun hashPassword(rawPassword: String): PasswordHash {
        return PasswordHash(passwordEncoder.encode(rawPassword))
    }

    fun validatePasswordStrength(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$")
        return password.matches(passwordRegex)
    }

    fun generateEmailVerificationToken(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun generatePasswordResetToken(): String {
        return java.util.UUID.randomUUID().toString()
    }

    fun generateTwoFactorSecret(): String {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 32)
    }

    fun calculateTokenExpiration(minutes: Int): LocalDateTime {
        return LocalDateTime.now().plusMinutes(minutes.toLong())
    }

    fun isTokenExpired(expiresAt: LocalDateTime): Boolean {
        return LocalDateTime.now().isAfter(expiresAt)
    }
}