package com.sleekydz86.komfa.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "password_reset_tokens")
data class PasswordResetTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(name = "token_value", nullable = false, unique = true, length = 255)
    val tokenValue: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
) {
    @Suppress("unused")
    constructor() : this(
        id = null,
        tokenValue = "",
        user = UserEntity(),
        expiresAt = Instant.now(),
    )
}
