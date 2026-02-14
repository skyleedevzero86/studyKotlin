package com.sleekydz86.komfa.infrastructure.persistence

import com.sleekydz86.komfa.domain.user.PasswordChangeHistoryEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PasswordChangeHistoryRepository : JpaRepository<PasswordChangeHistoryEntity, Long> {
    fun findByUserIdOrderByChangedAtDesc(userId: Long, pageable: Pageable): Page<PasswordChangeHistoryEntity>
}
