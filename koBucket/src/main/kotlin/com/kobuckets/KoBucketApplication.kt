package com.kobuckets

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KoBucketApplication

fun main(args: Array<String>) {
    runApplication<KoBucketApplication>(*args)
}
