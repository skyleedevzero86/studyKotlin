package com.karchitecture.auth.ui

import com.karchitecture.auth.ui.response.*
import com.karchitecture.auth.application.request.*
import com.karchitecture.auth.application.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/auth")
@RestController
class AuthController(
    private val authService: AuthService,
) : AuthApi {

    @PostMapping("/sign-up")
    override fun signUp(request: SignUpRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.signUp(request))
    }

    override fun signIn(request: SignInRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.signIn(request))
    }
}