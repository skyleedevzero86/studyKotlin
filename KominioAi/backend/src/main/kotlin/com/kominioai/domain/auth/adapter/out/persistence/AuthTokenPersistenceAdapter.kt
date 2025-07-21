package com.kominioai.domain.auth.adapter.out.persistence

import com.kominioai.domain.auth.application.port.out.LoadAuthTokenPort
import com.kominioai.domain.auth.application.port.out.SaveAuthTokenPort
import com.kominioai.domain.auth.domain.model.*
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Component
class AuthTokenPersistenceAdapter(
    private val client: DatabaseClient
) : LoadAuthTokenPort, SaveAuthTokenPort {

    override fun loadById(id: AuthTokenId): Mono<AuthToken?> =
        client.sql("SELECT * FROM auth_tokens WHERE id = :id")
            .bind("id", id.value)
            .map { row, _ -> rowToAuthToken(row) }
            .one()

    override fun loadByUserIdAndType(userId: UserId, type: TokenType): Flux<AuthToken> =
        client.sql("SELECT * FROM auth_tokens WHERE user_id = :userId AND token_type = :type")
            .bind("userId", userId.value)
            .bind("type", type.name)
            .map { row, _ -> rowToAuthToken(row) }
            .all()

    override fun loadByAccessToken(accessToken: String): Mono<AuthToken?> =
        client.sql("SELECT * FROM auth_tokens WHERE access_token = :accessToken")
            .bind("accessToken", accessToken)
            .map { row, _ -> rowToAuthToken(row) }
            .one()

    override fun loadByRefreshToken(refreshToken: String): Mono<AuthToken?> =
        client.sql("SELECT * FROM auth_tokens WHERE refresh_token = :refreshToken")
            .bind("refreshToken", refreshToken)
            .map { row, _ -> rowToAuthToken(row) }
            .one()

    override fun loadActiveTokensByUserId(userId: UserId): Flux<AuthToken> =
        client.sql("SELECT * FROM auth_tokens WHERE user_id = :userId AND revoked = false AND expires_at > :now")
            .bind("userId", userId.value)
            .bind("now", LocalDateTime.now())
            .map { row, _ -> rowToAuthToken(row) }
            .all()

    override fun save(token: AuthToken): Mono<AuthToken> {
        val sql = """
            INSERT INTO auth_tokens (id, user_id, token_type, access_token, refresh_token, expires_at, issued_at, device_info, ip_address, user_agent, revoked, revoked_at, revoked_reason)
            VALUES (:id, :userId, :tokenType, :accessToken, :refreshToken, :expiresAt, :issuedAt, :deviceInfo, :ipAddress, :userAgent, :revoked, :revokedAt, :revokedReason)
            ON CONFLICT (id) DO UPDATE SET
                access_token = EXCLUDED.access_token,
                refresh_token = EXCLUDED.refresh_token,
                expires_at = EXCLUDED.expires_at,
                revoked = EXCLUDED.revoked,
                revoked_at = EXCLUDED.revoked_at,
                revoked_reason = EXCLUDED.revoked_reason
        """.trimIndent()

        val bindSpec = client.sql(sql)
            .bind("id", token.id.value)
            .bind("userId", token.userId.value)
            .bind("tokenType", token.tokenType.name)
            .bind("accessToken", token.accessToken)
            .bind("expiresAt", token.expiresAt)
            .bind("issuedAt", token.issuedAt)
            .bind("revoked", token.revoked)

        // nullable 필드들을 조건부로 바인딩
        val finalBindSpec = bindSpec
            .let { spec ->
                if (token.refreshToken != null) spec.bind("refreshToken", token.refreshToken) else spec.bindNull("refreshToken", String::class.java)
            }
            .let { spec ->
                if (token.deviceInfo != null) spec.bind("deviceInfo", token.deviceInfo) else spec.bindNull("deviceInfo", String::class.java)
            }
            .let { spec ->
                if (token.ipAddress != null) spec.bind("ipAddress", token.ipAddress) else spec.bindNull("ipAddress", String::class.java)
            }
            .let { spec ->
                if (token.userAgent != null) spec.bind("userAgent", token.userAgent) else spec.bindNull("userAgent", String::class.java)
            }
            .let { spec ->
                if (token.revokedAt != null) spec.bind("revokedAt", token.revokedAt) else spec.bindNull("revokedAt", LocalDateTime::class.java)
            }
            .let { spec ->
                if (token.revokedReason != null) spec.bind("revokedReason", token.revokedReason) else spec.bindNull("revokedReason", String::class.java)
            }

        return finalBindSpec.then().thenReturn(token)
    }

    override fun revokeAllUserTokens(userId: UserId, reason: String): Mono<Void> =
        client.sql("UPDATE auth_tokens SET revoked = true, revoked_at = :now, revoked_reason = :reason WHERE user_id = :userId AND revoked = false")
            .bind("now", LocalDateTime.now())
            .bind("reason", reason)
            .bind("userId", userId.value)
            .then()

    override fun deleteExpiredTokens(): Mono<Long> =
        client.sql("DELETE FROM auth_tokens WHERE expires_at < :now")
            .bind("now", LocalDateTime.now())
            .fetch().rowsUpdated()
            .map { it.toLong() }

    override fun deleteById(id: String): Mono<Boolean> =
        client.sql("DELETE FROM auth_tokens WHERE id = :id")
            .bind("id", id)
            .fetch().rowsUpdated()
            .map { it > 0 }

    private fun rowToAuthToken(row: io.r2dbc.spi.Row): AuthToken {
        val id = row.get("id", String::class.java) ?: ""
        val userId = row.get("user_id", String::class.java) ?: ""
        val tokenType = row.get("token_type", String::class.java) ?: "ACCESS"
        val accessToken = row.get("access_token", String::class.java) ?: ""
        val refreshToken = row.get("refresh_token", String::class.java)
        val expiresAt = row.get("expires_at", LocalDateTime::class.java) ?: LocalDateTime.now()
        val issuedAt = row.get("issued_at", LocalDateTime::class.java) ?: LocalDateTime.now()
        val deviceInfo = row.get("device_info", String::class.java)
        val ipAddress = row.get("ip_address", String::class.java)
        val userAgent = row.get("user_agent", String::class.java)
        val revokedAt = row.get("revoked_at", LocalDateTime::class.java)
        val revokedReason = row.get("revoked_reason", String::class.java)

        // Boolean 타입을 명시적으로 변환
        val revokedValue = row.get("revoked", java.lang.Boolean::class.java)
        val revoked = revokedValue?.booleanValue() ?: false

        return AuthToken.reconstruct(
            id = id,
            userId = userId,
            tokenType = tokenType,
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