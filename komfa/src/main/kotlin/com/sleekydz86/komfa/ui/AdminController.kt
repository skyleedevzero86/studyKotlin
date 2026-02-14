package com.sleekydz86.komfa.ui

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin")
class AdminController {

    @GetMapping
    fun admin(@AuthenticationPrincipal principal: UserDetails?): ResponseEntity<Map<String, String>> {
        val name = principal?.username ?: "익명"
        return ResponseEntity.ok(mapOf("message" to "관리자 영역", "username" to name))
    }
}
