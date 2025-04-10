package com.karchitecture.auth.application.request

import io.swagger.v3.oas.annotations.media.Schema

data class SignInRequest(
    @Schema(
        description = "로그인 id",
        example = "root",
    )
    val username: String,

    @Schema(
        description = "로그인 패스워드",
        example = "root",
    )
    val password: String
)
