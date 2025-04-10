package com.karchitecture.auth.ui

import com.karchitecture.auth.application.request.SignInRequest
import com.karchitecture.auth.application.request.SignUpRequest
import com.karchitecture.auth.ui.response.AuthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Auth API")
interface AuthApi {

    @Operation(summary = "회원가입")
    @PostMapping("/sign-up")
    fun signUp(
        @RequestBody request: SignUpRequest
    ): ResponseEntity<AuthResponse>

    @Operation(summary = "로그인")
    @PostMapping("/sign-in")
    fun signIn(
        @RequestBody request: SignInRequest
    ): ResponseEntity<AuthResponse>
}