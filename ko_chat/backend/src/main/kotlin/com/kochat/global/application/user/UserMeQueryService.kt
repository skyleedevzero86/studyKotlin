package com.kochat.global.application.user

import com.kochat.domain.user.exception.UserNotFoundException
import com.kochat.domain.user.model.User
import com.kochat.domain.user.port.out.UserPersistencePort
import com.kochat.global.util.KoreanDateTimeFormatter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class UserMeQueryService(
    private val userPersistencePort: UserPersistencePort,
) {

    @Transactional(readOnly = true)
    fun findProfile(username: String): UserProfileResponse {
        val user = userPersistencePort.findByUsername(username)
            ?: throw UserNotFoundException(username)
        return user.toProfile()
    }

    private fun User.toProfile(): UserProfileResponse {
        val now = Instant.now()
        val daysUntilPasswordChange = PASSWORD_VALID_DAYS -
            ChronoUnit.DAYS.between(passwordChangedAt, now)

        return UserProfileResponse(
            username = username,
            displayName = displayName,
            role = role.name,
            status = status.name,
            createdAt = KoreanDateTimeFormatter.format(createdAt) ?: "",
            passwordChangedAt = KoreanDateTimeFormatter.format(passwordChangedAt) ?: "",
            passwordChangeFailCount = passwordChangeFailCount,
            loginFailCount = loginFailCount,
            lastLoginAt = KoreanDateTimeFormatter.format(lastLoginAt),
            passwordExpired = isPasswordExpired(now),
            daysUntilPasswordChange = daysUntilPasswordChange.coerceAtLeast(0),
        )
    }

    companion object {
        private const val PASSWORD_VALID_DAYS = User.PASSWORD_VALID_DAYS
    }
}
