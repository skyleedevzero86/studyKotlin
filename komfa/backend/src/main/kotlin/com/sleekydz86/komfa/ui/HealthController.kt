package com.sleekydz86.komfa.ui

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api")
class HealthController {

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> =
        ResponseEntity.ok(
            mapOf(
                "status" to "정상",
                "service" to "komfa-backend",
                "timestamp" to Instant.now().toString(),
            )
        )
}
