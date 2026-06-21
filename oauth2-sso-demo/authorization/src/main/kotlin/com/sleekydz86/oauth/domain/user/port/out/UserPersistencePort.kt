package com.sleekydz86.oauth.domain.user.port.out

import com.sleekydz86.oauth.domain.user.model.User

interface UserPersistencePort {
    fun save(user: User): User

    fun findByUsername(username: String): User?

    fun findAll(): List<User>

    fun existsByUsername(username: String): Boolean

    fun deleteByUsername(username: String)
}
