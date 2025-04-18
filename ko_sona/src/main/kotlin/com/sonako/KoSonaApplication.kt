package com.sonako

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KoSonaApplication

fun main(args: Array<String>) {
    runApplication<KoSonaApplication>(*args)
}