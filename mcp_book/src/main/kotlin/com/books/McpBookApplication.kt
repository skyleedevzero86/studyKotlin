package com.books

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class McpBookApplication

fun main(args: Array<String>) {
    runApplication<McpBookApplication>(*args)
}
