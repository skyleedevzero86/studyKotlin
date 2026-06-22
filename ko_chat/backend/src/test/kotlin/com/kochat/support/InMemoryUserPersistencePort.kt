package com.kochat.support

import com.kochat.domain.user.model.User
import com.kochat.domain.user.port.out.UserPersistencePort

class InMemoryUserPersistencePort : UserPersistencePort {

    private val users = linkedMapOf<String, User>()
    private var nextId = 1L

    override fun save(user: User): User {
        val saved = if (user.id == null) {
            User.restore(
                id = nextId++,
                username = user.username,
                password = user.password,
                displayName = user.displayName,
                role = user.role,
                status = user.status,
                createdAt = user.createdAt,
                passwordChangedAt = user.passwordChangedAt,
                passwordChangeFailCount = user.passwordChangeFailCount,
                loginFailCount = user.loginFailCount,
                lastLoginAt = user.lastLoginAt,
            )
        } else {
            user
        }
        users[saved.username] = saved
        return saved
    }

    override fun findByUsername(username: String): User? = users[username]

    override fun findAll(): List<User> = users.values.toList()

    override fun existsByUsername(username: String): Boolean = users.containsKey(username)

    override fun deleteByUsername(username: String) {
        users.remove(username)
    }
}
