package com.komroonga.domain.member.dto

data class MemberRequest(
    val username: String,
    val password: String,
    val name: String = "",
    val email: String = ""
)
