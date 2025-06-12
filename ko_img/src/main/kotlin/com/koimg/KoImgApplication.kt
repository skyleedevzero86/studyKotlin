package com.koimg

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KoImgApplication

fun main(args: Array<String>) {
    runApplication<KoImgApplication>(*args)
}
