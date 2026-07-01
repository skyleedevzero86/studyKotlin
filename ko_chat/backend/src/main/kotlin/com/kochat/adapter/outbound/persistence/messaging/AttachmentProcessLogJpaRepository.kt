package com.kochat.adapter.outbound.persistence.messaging

import org.springframework.data.jpa.repository.JpaRepository

interface AttachmentProcessLogJpaRepository : JpaRepository<AttachmentProcessLogJpaEntity, Long>
