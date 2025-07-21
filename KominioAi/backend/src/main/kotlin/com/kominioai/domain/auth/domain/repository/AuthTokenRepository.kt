package com.kominioai.domain.auth.domain.repository

import com.kominioai.domain.auth.domain.model.AuthToken
import com.kominioai.domain.auth.domain.model.AuthTokenId
import com.kominioai.domain.auth.domain.model.UserId
import com.kominioai.domain.auth.domain.model.TokenType
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface AuthTokenRepository {
    fun save(token: AuthToken): Mono<AuthToken>
    fun findById(id: AuthTokenId): Mono<AuthToken?>
    fun findByUserIdAndType(userId: UserId, type: TokenType): Flux<AuthToken>
    fun findByAccessToken(accessToken: String): Mono<AuthToken?>
    fun findByRefreshToken(refreshToken: String): Mono<AuthToken?>
    fun findActiveTokensByUserId(userId: UserId): Flux<AuthToken>
    fun revokeAllUserTokens(userId: UserId, reason: String): Mono<Void>
    fun deleteExpiredTokens(): Mono<Long>
    fun deleteById(id: AuthTokenId): Mono<Boolean>
}