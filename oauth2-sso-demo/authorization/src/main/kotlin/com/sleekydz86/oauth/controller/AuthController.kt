package com.sleekydz86.oauth.controller

import com.sleekydz86.oauth.dto.LoginRequest
import com.sleekydz86.oauth.dto.SignupRequest
import com.sleekydz86.oauth.dto.TokenResponse
import com.sleekydz86.oauth.util.JWTUtil
import com.sleekydz86.oauth.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JWTUtil,
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<Map<String, String>> {
        val user = userService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "username" to user.username,
                "role" to user.role.name,
            ),
        )
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): TokenResponse {
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password),
        )
        val role = authentication.authorities.first().authority.removePrefix("ROLE_")
        val accessToken = jwtUtil.createAccessToken(authentication.name, role)
        return TokenResponse(accessToken = accessToken)
    }
}
