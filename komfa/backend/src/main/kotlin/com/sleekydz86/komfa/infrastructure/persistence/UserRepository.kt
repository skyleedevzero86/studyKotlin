package com.sleekydz86.komfa.infrastructure.persistence

import com.sleekydz86.komfa.domain.user.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity?
    fun findFirstByEmailHash(emailHash: String): UserEntity?
    fun existsByEmailHash(emailHash: String): Boolean

    @Query(
        value = "SELECT * FROM public.users u WHERE :q IS NULL OR LOWER(u.username) LIKE LOWER('%' || CAST(:q AS text) || '%')",
        countQuery = "SELECT COUNT(*) FROM public.users u WHERE :q IS NULL OR LOWER(u.username) LIKE LOWER('%' || CAST(:q AS text) || '%')",
        nativeQuery = true,
    )
    fun findAllBySearch(q: String?, pageable: Pageable): Page<UserEntity>
}
