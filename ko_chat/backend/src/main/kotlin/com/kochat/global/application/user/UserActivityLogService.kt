package com.kochat.global.application.user

import com.kochat.adapter.outbound.persistence.user.UserActivityLogJpaEntity
import com.kochat.adapter.outbound.persistence.user.UserActivityLogJpaRepository
import com.kochat.adapter.outbound.persistence.user.UserJpaRepository
import com.kochat.domain.user.model.UserActivityEventType
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class UserActivityLogService(
    private val userActivityLogJpaRepository: UserActivityLogJpaRepository,
    private val userJpaRepository: UserJpaRepository,
) {
    @Transactional
    fun record(userId: Long, eventType: UserActivityEventType, actorId: Long? = null, occurredAt: Instant = Instant.now()) {
        userActivityLogJpaRepository.save(
            UserActivityLogJpaEntity().apply {
                this.userId = userId
                this.eventType = eventType
                this.actorId = actorId
                this.occurredAt = toLocalDateTime(occurredAt)
            },
        )
    }

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun backfillIfEmpty() {
        if (userActivityLogJpaRepository.count() > 0) return

        val logs = mutableListOf<UserActivityLogJpaEntity>()
        userJpaRepository.findAll().forEach { user ->
            val userId = user.id ?: return@forEach
            val createdAt = user.createdAt ?: return@forEach
            logs += UserActivityLogJpaEntity().apply {
                this.userId = userId
                eventType = UserActivityEventType.JOIN
                occurredAt = toLocalDateTime(createdAt)
            }

            val passwordChangedAt = user.passwordChangedAt ?: return@forEach
            if (passwordChangedAt.isAfter(createdAt.plusSeconds(1))) {
                logs += UserActivityLogJpaEntity().apply {
                    this.userId = userId
                    eventType = UserActivityEventType.PASSWORD_CHANGE
                    occurredAt = toLocalDateTime(passwordChangedAt)
                }
            }
        }

        if (logs.isNotEmpty()) {
            userActivityLogJpaRepository.saveAll(logs)
        }
    }

    private fun toLocalDateTime(instant: Instant): LocalDateTime =
        LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
}
