package com.kominioai.domain.auth.application.port.out

import com.kominioai.domain.auth.domain.model.User
import com.kominioai.domain.auth.domain.model.UserId
import com.kominioai.domain.auth.domain.model.Email
import com.kominioai.domain.auth.domain.model.Username
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface LoadUserPort {
    fun loadById(id: UserId): Mono<User?>
    fun loadByEmail(email: Email): Mono<User?>
    fun loadByUsername(username: Username): Mono<User?>
    fun loadByEmailOrUsername(emailOrUsername: String): Mono<User?>
    fun loadAll(page: Int, size: Int): Flux<User>
    fun loadByAccountStatus(status: String, page: Int, size: Int): Flux<User>
    fun searchUsers(query: String, page: Int, size: Int): Flux<User>
    fun existsByEmail(email: Email): Mono<Boolean>
    fun existsByUsername(username: Username): Mono<Boolean>
    fun countByAccountStatus(status: String): Mono<Long>
    fun countTotalUsers(): Mono<Long>
}