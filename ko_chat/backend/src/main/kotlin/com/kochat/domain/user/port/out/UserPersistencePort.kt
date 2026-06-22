package com.kochat.domain.user.port.out

import com.kochat.domain.user.model.User

interface UserPersistencePort {
    fun save(user: User): User

    fun findByUsername(username: String): User?

    fun findAll(): List<User>

    fun existsByUsername(username: String): Boolean

    fun deleteByUsername(username: String)
}
