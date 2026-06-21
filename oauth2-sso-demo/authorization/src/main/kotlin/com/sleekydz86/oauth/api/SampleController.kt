package com.sleekydz86.oauth.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class SampleController {

    @GetMapping("/")
    fun index(): String = "index"

    @GetMapping("/user")
    fun user(): String = "user"

    @GetMapping("/admin")
    fun admin(): String = "admin"
}
