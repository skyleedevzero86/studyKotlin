package com.kominioai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class KominioAiApplication

fun main(args: Array<String>) {
    runApplication<KominioAiApplication>(*args)
}
