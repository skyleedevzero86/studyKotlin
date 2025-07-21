package com.kominioai.domain.auth.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class AuthToken(
    val id: AuthTokenId,
    val userId: UserId,
    val tokenType: TokenType,
    val accessToken: String,
    val refreshToken: String?,
    val expiresAt: LocalDateTime,
    val issuedAt: LocalDateTime,
    val deviceInfo: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val revoked: Boolean,
    val revokedAt: LocalDateTime?,
    val revokedReason: String?
) {
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun isRevoked(): Boolean = revoked

    fun revoke(reason: String): AuthToken {
        return copy(
            revoked = true,
            revokedAt = LocalDateTime.now(),
            revokedReason = reason
        )
    }

    fun isValid(): Boolean = !isExpired() && !isRevoked()

    companion object {
        fun create(
            userId: UserId,
            tokenType: TokenType,
            accessToken: String,
            refreshToken: String? = null,
            expiresAt: LocalDateTime,
            deviceInfo: String? = null,
            ipAddress: String? = null,
            userAgent: String? = null
        ): AuthToken {
            val now = LocalDateTime.now()
            return AuthToken(
                id = AuthTokenId(UUID.randomUUID().toString()),
                userId = userId,
                tokenType = tokenType,
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                issuedAt = now,
                deviceInfo = deviceInfo,
                ipAddress = ipAddress,
                userAgent = userAgent,
                revoked = false,
                revokedAt = null,
                revokedReason = null
            )
        }

        fun reconstruct(
            id: String,
            userId: String,
            tokenType: String,
            accessToken: String,
            refreshToken: String?,
            expiresAt: LocalDateTime,
            issuedAt: LocalDateTime,
            deviceInfo: String?,
            ipAddress: String?,
            userAgent: String?,
            revoked: Boolean,
            revokedAt: LocalDateTime?,
            revokedReason: String?
        ): AuthToken {
            return AuthToken(
                id = AuthTokenId(id),
                userId = UserId(userId),
                tokenType = TokenType.valueOf(tokenType),
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                issuedAt = issuedAt,
                deviceInfo = deviceInfo,
                ipAddress = ipAddress,
                userAgent = userAgent,
                revoked = revoked,
                revokedAt = revokedAt,
                revokedReason = revokedReason
            )
        }
    }
}