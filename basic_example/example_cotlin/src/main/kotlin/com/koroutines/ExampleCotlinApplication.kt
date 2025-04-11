package com.koroutines

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ExampleCotlinApplication

fun main(args: Array<String>) {
    runApplication<ExampleCotlinApplication>(*args)
}
