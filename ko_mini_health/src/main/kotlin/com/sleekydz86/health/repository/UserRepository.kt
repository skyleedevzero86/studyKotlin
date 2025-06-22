package com.sleekydz86.health.repository

import com.sleekydz86.health.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, String> {
    fun findByUserId(userId: String): User?
}