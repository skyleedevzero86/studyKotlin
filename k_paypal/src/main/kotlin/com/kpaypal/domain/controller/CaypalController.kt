package com.kpaypal.domain.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Controller
@RequestMapping("/load")
class CaypalController {

    @GetMapping("/")
    fun home(): String {
        return "pay/payment"
    }
}