package com.komroonga.domain.member.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class MemberResponse @JsonCreator constructor(
    @JsonProperty("id") val id: Long,
    @JsonProperty("username") val username: String
)