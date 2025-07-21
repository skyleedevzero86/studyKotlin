package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.domain.model.UserSocialAccount
import com.kominioai.domain.auth.domain.model.UserId
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface LoadUserSocialAccountPort {
    fun loadByUserId(userId: UserId): Flux<UserSocialAccount>
    fun loadByProviderAndProviderUserId(provider: String, providerUserId: String): Mono<UserSocialAccount?>
    fun loadActiveAccountsByUserId(userId: UserId): Flux<UserSocialAccount>
}