package com.chaorks

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GorksApplication

fun main(args: Array<String>) {
    runApplication<GorksApplication>(*args)
}
