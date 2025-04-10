package com.kpaypal

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KPaypalApplication

fun main(args: Array<String>) {
    runApplication<KPaypalApplication>(*args)
}
