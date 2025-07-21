package com.kominioai.domain.auth.adapter.out.persistence

import com.kominioai.domain.auth.application.port.out.LoadUserSocialAccountPort
import com.kominioai.domain.auth.application.port.out.SaveUserSocialAccountPort
import com.kominioai.domain.auth.domain.model.*
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Component
class UserSocialAccountPersistenceAdapter(
    private val client: DatabaseClient
) : LoadUserSocialAccountPort, SaveUserSocialAccountPort {

    override fun loadByUserId(userId: UserId): Flux<UserSocialAccount> =
        client.sql("SELECT * FROM user_social_accounts WHERE user_id = :userId")
            .bind("userId", userId.value)
            .map { row, _ -> rowToUserSocialAccount(row) }
            .all()

    override fun loadByProviderAndProviderUserId(provider: String, providerUserId: String): Mono<UserSocialAccount?> =
        client.sql("SELECT * FROM user_social_accounts WHERE provider = :provider AND provider_user_id = :providerUserId")
            .bind("provider", provider)
            .bind("providerUserId", providerUserId)
            .map { row, _ -> rowToUserSocialAccount(row) }
            .one()

    override fun loadActiveAccountsByUserId(userId: UserId): Flux<UserSocialAccount> =
        client.sql("SELECT * FROM user_social_accounts WHERE user_id = :userId AND active = true")
            .bind("userId", userId.value)
            .map { row, _ -> rowToUserSocialAccount(row) }
            .all()

    override fun save(account: UserSocialAccount): Mono<UserSocialAccount> {
        val sql = """
            INSERT INTO user_social_accounts (id, user_id, provider, provider_user_id, email, display_name, profile_image_url, access_token, refresh_token, token_expires_at, connected_at, last_synced_at, active)
            VALUES (:id, :userId, :provider, :providerUserId, :email, :displayName, :profileImageUrl, :accessToken, :refreshToken, :tokenExpiresAt, :connectedAt, :lastSyncedAt, :active)
            ON CONFLICT (provider, provider_user_id) DO UPDATE SET
                user_id = EXCLUDED.user_id,
                email = EXCLUDED.email,
                display_name = EXCLUDED.display_name,
                profile_image_url = EXCLUDED.profile_image_url,
                access_token = EXCLUDED.access_token,
                refresh_token = EXCLUDED.refresh_token,
                token_expires_at = EXCLUDED.token_expires_at,
                last_synced_at = EXCLUDED.last_synced_at,
                active = EXCLUDED.active
        """.trimIndent()

        val bindSpec = client.sql(sql)
            .bind("id", account.id.value)
            .bind("userId", account.userId.value)
            .bind("provider", account.provider.name)
            .bind("providerUserId", account.providerUserId)
            .bind("active", account.active)
            .bind("connectedAt", account.connectedAt)

        val finalBindSpec = bindSpec
            .let { spec ->
                if (account.email != null) spec.bind("email", account.email) else spec.bindNull("email", String::class.java)
            }
            .let { spec ->
                if (account.displayName != null) spec.bind("displayName", account.displayName) else spec.bindNull("displayName", String::class.java)
            }
            .let { spec ->
                if (account.profileImageUrl != null) spec.bind("profileImageUrl", account.profileImageUrl) else spec.bindNull("profileImageUrl", String::class.java)
            }
            .let { spec ->
                if (account.accessToken != null) spec.bind("accessToken", account.accessToken) else spec.bindNull("accessToken", String::class.java)
            }
            .let { spec ->
                if (account.refreshToken != null) spec.bind("refreshToken", account.refreshToken) else spec.bindNull("refreshToken", String::class.java)
            }
            .let { spec ->
                if (account.tokenExpiresAt != null) spec.bind("tokenExpiresAt", account.tokenExpiresAt) else spec.bindNull("tokenExpiresAt", LocalDateTime::class.java)
            }
            .let { spec ->
                if (account.lastSyncedAt != null) spec.bind("lastSyncedAt", account.lastSyncedAt) else spec.bindNull("lastSyncedAt", LocalDateTime::class.java)
            }

        return finalBindSpec.then().thenReturn(account)
    }

    override fun deleteById(id: String): Mono<Boolean> =
        client.sql("DELETE FROM user_social_accounts WHERE id = :id")
            .bind("id", id)
            .fetch().rowsUpdated()
            .map { it > 0 }

    private fun rowToUserSocialAccount(row: io.r2dbc.spi.Row): UserSocialAccount {
        val id = row.get("id", String::class.java) ?: ""
        val userId = row.get("user_id", String::class.java) ?: ""
        val provider = row.get("provider", String::class.java) ?: "GOOGLE"
        val providerUserId = row.get("provider_user_id", String::class.java) ?: ""
        val email = row.get("email", String::class.java)
        val displayName = row.get("display_name", String::class.java)
        val profileImageUrl = row.get("profile_image_url", String::class.java)
        val accessToken = row.get("access_token", String::class.java)
        val refreshToken = row.get("refresh_token", String::class.java)
        val tokenExpiresAt = row.get("token_expires_at", LocalDateTime::class.java)
        val connectedAt = row.get("connected_at", LocalDateTime::class.java) ?: LocalDateTime.now()
        val lastSyncedAt = row.get("last_synced_at", LocalDateTime::class.java)

        val activeValue = row.get("active", java.lang.Boolean::class.java)
        val active = activeValue?.booleanValue() ?: true

        return UserSocialAccount.reconstruct(
            id = id,
            userId = userId,
            provider = provider,
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