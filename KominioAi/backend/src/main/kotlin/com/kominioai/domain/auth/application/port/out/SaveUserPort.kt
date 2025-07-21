package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.domain.model.User
import reactor.core.publisher.Mono

interface SaveUserPort {
    fun save(user: User): Mono<User>
    fun deleteById(id: String): Mono<Boolean>
}