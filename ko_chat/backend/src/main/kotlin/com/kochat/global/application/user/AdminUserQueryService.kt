package com.kochat.global.application.user

import com.kochat.domain.user.model.User
import com.kochat.domain.user.port.out.UserPersistencePort
import com.kochat.global.crypto.AesEncryptionService
import com.kochat.global.util.KoreanDateTimeFormatter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class AdminUserQueryService(
    private val userPersistencePort: UserPersistencePort,
    private val aesEncryptionService: AesEncryptionService,
    private val objectMapper: ObjectMapper,
) {

    @Transactional(readOnly = true)
    fun findAllUserSummaries(): List<UserSummaryResponse> =
        userPersistencePort.findAll().map { it.toEncryptedSummary() }

    @Transactional(readOnly = true)
    fun findUserSummaries(pageable: Pageable): Page<UserSummaryResponse> =
        userPersistencePort.findAll(pageable).map { it.toEncryptedSummary() }

    private fun User.toEncryptedSummary(): UserSummaryResponse {
        val now = Instant.now()
        val daysUntilPasswordChange = User.PASSWORD_VALID_DAYS -
            ChronoUnit.DAYS.between(passwordChangedAt, now)

        val sensitivePayload = UserSensitivePayload(
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

        val json = objectMapper.writeValueAsString(sensitivePayload)
        return UserSummaryResponse(
            username = username,
            encryptedPayload = aesEncryptionService.encrypt(json),
        )
    }
}
