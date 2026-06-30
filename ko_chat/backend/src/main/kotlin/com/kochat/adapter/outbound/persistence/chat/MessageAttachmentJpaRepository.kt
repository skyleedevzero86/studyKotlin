package com.kochat.adapter.outbound.persistence.chat

import org.springframework.data.jpa.repository.JpaRepository

interface MessageAttachmentJpaRepository : JpaRepository<MessageAttachmentJpaEntity, Long> {
    fun findByObjectKey(objectKey: String): MessageAttachmentJpaEntity?
}
