package com.sleekydz86.health.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class RouteController {

    @GetMapping("/health")
    fun health(): String {
        return "health"
    }
}