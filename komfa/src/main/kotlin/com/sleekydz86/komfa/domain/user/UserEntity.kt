package com.sleekydz86.komfa.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true, length = 64)
    val username: String,
    @Column(name = "password_hash", nullable = false, length = 255)
    val passwordHash: String,
    @Column(name = "email", nullable = true, length = 512)
    val email: String? = null,
    @Column(name = "email_hash", nullable = true, length = 64)
    val emailHash: String? = null,
    @Column(nullable = false, length = 64)
    val roles: String,
    @Column(name = "status", nullable = false, length = 32)
    val status: String = UserStatus.PENDING.name,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
) {
    @Suppress("unused")
    constructor() : this(
        id = null,
        username = "",
        passwordHash = "",
        email = null,
        emailHash = null,
        roles = "",
        status = UserStatus.PENDING.name,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )
}
