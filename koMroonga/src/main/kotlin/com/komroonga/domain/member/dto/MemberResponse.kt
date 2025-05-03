package com.komroonga.domain.member.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.komroonga.member.entity.Role

/**
 * 회원 응답 DTO
 */
data class MemberResponse @JsonCreator constructor(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("email") val email: String? = null,
    @JsonProperty("role") val role: Role = Role.ROLE_USER
)