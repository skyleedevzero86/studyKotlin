package com.sleekydz86.oauth.controller

import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ResourceController {

    @GetMapping("/api/user/me")
    fun userResource(authentication: Authentication): Map<String, Any> =
        mapOf(
            "message" to "USER resource",
            "username" to authentication.name,
            "authorities" to authentication.authorities.map { it.authority },
        )

    @GetMapping("/api/admin/dashboard")
    fun adminResource(authentication: Authentication): Map<String, Any> =
        mapOf(
            "message" to "ADMIN resource",
            "username" to authentication.name,
            "authorities" to authentication.authorities.map { it.authority },
        )
}
