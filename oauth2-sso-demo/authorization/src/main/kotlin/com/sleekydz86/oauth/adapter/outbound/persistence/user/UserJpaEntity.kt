package com.sleekydz86.oauth.adapter.outbound.persistence.user

import com.sleekydz86.oauth.domain.user.model.UserRole
import com.sleekydz86.oauth.domain.user.model.UserStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    var username: String? = null

    @Column(nullable = false)
    var password: String? = null

    var displayName: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus? = null

    @Column(nullable = false)
    var createdAt: Instant? = null

    @Column(nullable = false)
    var passwordChangedAt: Instant? = null

    @Column(nullable = false)
    var passwordChangeFailCount: Int = 0

    @Column(nullable = false)
    var loginFailCount: Int = 0

    var lastLoginAt: Instant? = null
}
