package com.karchitecture.auth.ui

import com.karchitecture.auth.application.request.*
import com.karchitecture.auth.application.*
import com.karchitecture.auth.ui.response.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/auth")
@RestController
class AuthController(
    private val authService: AuthService,
) : AuthApi {

    @PostMapping("/sign-up")
    override fun signUp(request: SignUpRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.signUp(request))
    }

    @GetMapping("/sign-in")
    override fun signIn(request: SignInRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.signIn(request))
    }
}
