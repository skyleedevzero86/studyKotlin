package com.kominioai.domain.auth.domain.repository

import com.kominioai.domain.auth.domain.model.User
import com.kominioai.domain.auth.domain.model.UserId
import com.kominioai.domain.auth.domain.model.Email
import com.kominioai.domain.auth.domain.model.Username
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

interface UserRepository {
    fun save(user: User): Mono<User>
    fun findById(id: UserId): Mono<User?>
    fun findByEmail(email: Email): Mono<User?>
    fun findByUsername(username: Username): Mono<User?>
    fun findByEmailOrUsername(emailOrUsername: String): Mono<User?>
    fun existsByEmail(email: Email): Mono<Boolean>
    fun existsByUsername(username: Username): Mono<Boolean>
    fun findAll(page: Int, size: Int): Flux<User>
    fun findByAccountStatus(status: String, page: Int, size: Int): Flux<User>
    fun searchUsers(query: String, page: Int, size: Int): Flux<User>
    fun deleteById(id: UserId): Mono<Boolean>
    fun countByAccountStatus(status: String): Mono<Long>
    fun countTotalUsers(): Mono<Long>
}