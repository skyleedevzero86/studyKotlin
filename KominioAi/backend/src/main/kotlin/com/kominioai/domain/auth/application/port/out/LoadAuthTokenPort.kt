package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.domain.model.AuthToken
import com.kominioai.domain.auth.domain.model.AuthTokenId
import com.kominioai.domain.auth.domain.model.UserId
import com.kominioai.domain.auth.domain.model.TokenType
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface LoadAuthTokenPort {
    fun loadById(id: AuthTokenId): Mono<AuthToken?>
    fun loadByUserIdAndType(userId: UserId, type: TokenType): Flux<AuthToken>
    fun loadByAccessToken(accessToken: String): Mono<AuthToken?>
    fun loadByRefreshToken(refreshToken: String): Mono<AuthToken?>
    fun loadActiveTokensByUserId(userId: UserId): Flux<AuthToken>
}