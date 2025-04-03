package com.karchitecture

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LayeredBasicApplication

fun main(args: Array<String>) {
    runApplication<LayeredBasicApplication>(*args)
}
