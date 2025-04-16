package com.kochat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KoChatApplication

fun main(args: Array<String>) {
    runApplication<KoChatApplication>(*args)
}
