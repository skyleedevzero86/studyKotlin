package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.domain.model.AuthToken
import com.kominioai.domain.auth.domain.model.UserId
import reactor.core.publisher.Mono

interface SaveAuthTokenPort {
    fun save(token: AuthToken): Mono<AuthToken>
    fun revokeAllUserTokens(userId: UserId, reason: String): Mono<Void>
    fun deleteExpiredTokens(): Mono<Long>
    fun deleteById(id: String): Mono<Boolean>
}