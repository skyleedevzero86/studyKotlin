package com.komroonga.member.entity

import jakarta.persistence.*
import org.hibernate.annotations.Index
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(
    name = "member",
    indexes = [
        Index(name = "idx_member_username", columnList = "username")
    ]
)
data class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    private val username: String,

    @Column(nullable = false)
    private val password: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: Role = Role.ROLE_USER,

    @Column(nullable = true)
    val email: String? = null,

    @Column(nullable = true)
    val name: String? = null
) : UserDetails {
    init {
        require(username.isNotBlank()) { "사용자 이름은 비어 있을 수 없습니다" }
        require(password.isNotBlank()) { "비밀번호는 비어 있을 수 없습니다" }
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role.name))
    }

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}

enum class Role {
    ROLE_USER, ROLE_ADMIN
}
