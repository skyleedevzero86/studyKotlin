package com.chaorks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class GorksApplication

fun main(args: Array<String>) {
    runApplication<GorksApplication>(*args)
}
