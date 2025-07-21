package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.domain.model.UserSocialAccount
import reactor.core.publisher.Mono

interface SaveUserSocialAccountPort {
    fun save(account: UserSocialAccount): Mono<UserSocialAccount>
    fun deleteById(id: String): Mono<Boolean>
}