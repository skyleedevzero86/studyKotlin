package com.kpaypal.domain.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/paypal")
class WebController {
    @GetMapping("/cancel")
    fun cancel(): String = "payment canceled"
}