package com.karchitecture.auth.ui

import com.karchitecture.auth.application.request.*
import com.karchitecture.auth.application.*
import com.karchitecture.auth.ui.response.*
import com.karchitecture.global.annotation.AuthMember
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/auth")
@RestController
class AuthController(
    private val authService: AuthService,
) : AuthApi {

    @PostMapping("/sign-up")
    override fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<AuthResponse> {
        println("request: "+request.toString())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.signUp(request))
    }

    @GetMapping("/sign-in")
    override fun signIn(@RequestBody request: SignInRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.signIn(request))
    }

    @Operation(summary = "test")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/test")
    fun test(@AuthMember id: Long): String {
        return "hi $id"
    }
}
