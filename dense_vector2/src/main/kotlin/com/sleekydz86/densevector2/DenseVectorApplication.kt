package com.sleekydz86.densevector2

import com.sleekydz86.densevector2.global.config.VectorProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(VectorProperties::class)
class DenseVectorApplication

fun main(args: Array<String>) {
    runApplication<DenseVectorApplication>(*args)
}
