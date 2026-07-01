package com.kochat.adapter.outbound.persistence.messaging

import org.springframework.data.jpa.repository.JpaRepository

interface MessageSearchIndexJpaRepository : JpaRepository<MessageSearchIndexJpaEntity, Long>
