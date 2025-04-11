package com.karchitecture.auth.application.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema

@JsonIgnoreProperties(ignoreUnknown = true)
data class SignUpRequest(
    @Schema(
        description = "회원가입 id",
        example = "root",
    )
    val username: String,

    @Schema(
        description = "회원가입 패스워드",
        example = "root",
    )
    val password: String
)