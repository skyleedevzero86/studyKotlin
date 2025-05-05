package com.komroonga

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class KoMroongaApplication

fun main(args: Array<String>) {
    runApplication<KoMroongaApplication>(*args)
}
