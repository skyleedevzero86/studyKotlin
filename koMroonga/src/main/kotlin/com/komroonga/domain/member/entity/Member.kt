package com.komroonga.member.entity

import jakarta.persistence.*
import org.hibernate.annotations.Index
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Member 엔티티 클래스
 * 함수형 프로그래밍 스타일로 구현
 * UserDetails 인터페이스 충돌 해결
 */
@Entity
@Table(
    name = "member",
    indexes = [
        Index(name = "idx_member_username", columnList = "username")
    ]
)
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    private val username: String,

    @Column(nullable = false)
    private val password: String,

    @Column(nullable = true)
    val name: String? = "",

    @Column(nullable = true)
    val email: String? = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.ROLE_USER
) : UserDetails {

    // 초기 검증 로직
    init {
        require(username.isNotBlank()) { "사용자 이름은 비어 있을 수 없습니다" }
        require(password.isNotBlank()) { "비밀번호는 비어 있을 수 없습니다" }
    }

    // UserDetails 인터페이스 구현
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority(role.name))

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    // 함수형 스타일 유틸리티 메서드들
    fun copy(
        id: Long? = this.id,
        username: String = this.username,
        password: String = this.password,
        name: String? = this.name,
        email: String? = this.email,
        role: Role = this.role
    ): Member = Member(id, username, password, name, email, role)

    // equals, hashCode, toString 구현
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false

        return id == other.id &&
                username == other.username &&
                name == other.name &&
                email == other.email &&
                role == other.role
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + username.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (email?.hashCode() ?: 0)
        result = 31 * result + role.hashCode()
        return result
    }

    override fun toString(): String =
        "Member(id=$id, username=$username, name=$name, email=$email, role=$role)"


    companion object {
        fun create(
            username: String,
            password: String,
            name: String? = "",
            email: String? = "",
            role: Role = Role.ROLE_USER
        ): Member = Member(
            username = username,
            password = password,
            name = name,
            email = email,
            role = role
        )

        fun hasAdminRole(member: Member): Boolean =
            member.role == Role.ROLE_ADMIN
    }
}

enum class Role {
    ROLE_USER, ROLE_ADMIN
}