package com.kominioai.domain.auth.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class UserSocialAccount(
    val id: UserSocialAccountId,
    val userId: UserId,
    val provider: SocialProvider,
    val providerUserId: String,
    val email: String?,
    val displayName: String?,
    val profileImageUrl: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val tokenExpiresAt: LocalDateTime?,
    val connectedAt: LocalDateTime,
    val lastSyncedAt: LocalDateTime?,
    val active: Boolean
) {
    fun deactivate(): UserSocialAccount {
        return copy(active = false)
    }

    fun activate(): UserSocialAccount {
        return copy(active = true)
    }

    fun updateTokens(
        accessToken: String?,
        refreshToken: String?,
        tokenExpiresAt: LocalDateTime?
    ): UserSocialAccount {
        return copy(
            accessToken = accessToken,
            refreshToken = refreshToken,
            tokenExpiresAt = tokenExpiresAt,
            lastSyncedAt = LocalDateTime.now()
        )
    }

    fun updateProfileInfo(
        email: String?,
        displayName: String?,
        profileImageUrl: String?
    ): UserSocialAccount {
        return copy(
            email = email,
            displayName = displayName,
            profileImageUrl = profileImageUrl,
            lastSyncedAt = LocalDateTime.now()
        )
    }

    companion object {
        fun create(
            userId: UserId,
            provider: SocialProvider,
            providerUserId: String,
            email: String? = null,
            displayName: String? = null,
            profileImageUrl: String? = null,
            accessToken: String? = null,
            refreshToken: String? = null,
            tokenExpiresAt: LocalDateTime? = null
        ): UserSocialAccount {
            return UserSocialAccount(
                id = UserSocialAccountId(UUID.randomUUID().toString()),
                userId = userId,
                provider = provider,
                providerUserId = providerUserId,
                email = email,
                displayName = displayName,
                profileImageUrl = profileImageUrl,
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenExpiresAt = tokenExpiresAt,
                connectedAt = LocalDateTime.now(),
                lastSyncedAt = null,
                active = true
            )
        }

        fun reconstruct(
            id: String,
            userId: String,
            provider: String,
            providerUserId: String,
            email: String?,
            displayName: String?,
            profileImageUrl: String?,
            accessToken: String?,
            refreshToken: String?,
            tokenExpiresAt: LocalDateTime?,
            connectedAt: LocalDateTime,
            lastSyncedAt: LocalDateTime?,
            active: Boolean
        ): UserSocialAccount {
            return UserSocialAccount(
                id = UserSocialAccountId(id),
                userId = UserId(userId),
                provider = SocialProvider.valueOf(provider),
                providerUserId = providerUserId,
                email = email,
                displayName = displayName,
                profileImageUrl = profileImageUrl,
                accessToken = accessToken,
                refreshToken = refreshToken,
                tokenExpiresAt = tokenExpiresAt,
                connectedAt = connectedAt,
                lastSyncedAt = lastSyncedAt,
                active = active
            )
        }
    }
}