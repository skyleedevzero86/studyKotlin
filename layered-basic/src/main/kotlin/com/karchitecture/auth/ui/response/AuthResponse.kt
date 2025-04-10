package com.karchitecture.auth.ui.response

import io.swagger.v3.oas.annotations.media.Schema

data class AuthResponse(
    @Schema(
        description = "Access token",
        example = "tokeninfo123123...asdvio3"
    )
    val accessToken: String
) {
}