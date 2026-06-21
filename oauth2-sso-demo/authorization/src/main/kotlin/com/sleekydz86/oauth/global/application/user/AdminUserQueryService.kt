package com.sleekydz86.oauth.global.application.user

import com.sleekydz86.oauth.domain.user.model.User
import com.sleekydz86.oauth.domain.user.port.out.UserPersistencePort
import com.sleekydz86.oauth.global.crypto.AesEncryptionService
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

    private fun User.toEncryptedSummary(): UserSummaryResponse {
        val now = Instant.now()
        val daysUntilPasswordChange = User.PASSWORD_VALID_DAYS -
            ChronoUnit.DAYS.between(passwordChangedAt, now)

        val sensitivePayload = UserSensitivePayload(
            role = role.name,
            status = status.name,
            createdAt = createdAt.toString(),
            passwordChangedAt = passwordChangedAt.toString(),
            passwordChangeFailCount = passwordChangeFailCount,
            lastLoginAt = lastLoginAt?.toString(),
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
