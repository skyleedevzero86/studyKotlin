package com.sleekydz86.komfa.ui

import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.util.UriComponentsBuilder

@Controller
class LoginRedirectController(
    @Value("\${komfa.frontend.base-url:http://localhost:5173}") private val frontendBaseUrl: String,
) {

    @GetMapping("/login")
    fun redirectToFrontendLogin(request: HttpServletRequest): ResponseEntity<Void> {
        val queryString = request.queryString
        val builder = UriComponentsBuilder.fromUriString("$frontendBaseUrl/login")
        if (!queryString.isNullOrBlank()) builder.replaceQuery(queryString)
        val location = builder.build().toUri()
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build()
    }

    @GetMapping("/login/ott")
    fun redirectToFrontendLoginOtt(request: HttpServletRequest): ResponseEntity<Void> {
        val queryString = request.queryString
        val builder = UriComponentsBuilder.fromUriString("$frontendBaseUrl/login/ott")
        if (!queryString.isNullOrBlank()) builder.replaceQuery(queryString)
        val location = builder.build().toUri()
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build()
    }

    @GetMapping("/reset-password")
    fun redirectToFrontendResetPassword(request: HttpServletRequest): ResponseEntity<Void> {
        val queryString = request.queryString
        val builder = UriComponentsBuilder.fromUriString("$frontendBaseUrl/reset-password")
        if (!queryString.isNullOrBlank()) builder.replaceQuery(queryString)
        val location = builder.build().toUri()
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build()
    }
}
