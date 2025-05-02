package com.komroonga.domain.member.dto

import com.komroonga.member.entity.Role

data class MemberRequest(
    val username: String,
    val password: String,
    val name: String = "",
    val email: String = "",
    val role: Role = Role.ROLE_USER
)
