package com.komroonga.member.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id

@Entity
data class Member(
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val password: String
) {
    init {
        require(username.isNotBlank()) { "사용자 이름은 비어 있을 수 없습니다" }
        require(password.isNotBlank()) { "비밀번호는 비어 있을 수 없습니다" }
    }
}