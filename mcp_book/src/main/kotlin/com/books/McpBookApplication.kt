package com.books

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class McpBookApplication

fun main(args: Array<String>) {
    runApplication<McpBookApplication>(*args)
}
