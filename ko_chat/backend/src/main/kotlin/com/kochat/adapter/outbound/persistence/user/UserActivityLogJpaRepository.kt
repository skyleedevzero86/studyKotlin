package com.kochat.adapter.outbound.persistence.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserActivityLogJpaRepository : JpaRepository<UserActivityLogJpaEntity, Long>
