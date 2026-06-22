package com.kochat.adapter.outbound.persistence.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByUsername(username: String): UserJpaEntity?

    fun existsByUsername(username: String): Boolean

    fun deleteByUsername(username: String)
}
