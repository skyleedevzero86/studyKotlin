package com.kstomp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KStompApplication

fun main(args: Array<String>) {
    runApplication<KStompApplication>(*args)
}
