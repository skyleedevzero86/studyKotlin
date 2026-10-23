package com.sleekydz86.multitenant.persistence.casting

import org.springframework.data.jpa.repository.JpaRepository

interface CastingJpaRepository : JpaRepository<CastingEntity, String>
