package com.sleekydz86.komfa.domain.ott

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "one_time_tokens")
data class OneTimeTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "token_value", nullable = false, unique = true, length = 255)
    val tokenValue: String,
    @Column(nullable = false, length = 64)
    val username: String,
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
) {
    @Suppress("unused")
    constructor() : this(
        id = null,
        tokenValue = "",
        username = "",
        expiresAt = Instant.now(),
    )
}
