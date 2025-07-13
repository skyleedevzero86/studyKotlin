package com.kominioai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@EnableScheduling
@ComponentScan(
    basePackages = ["com.kominioai"],
    excludeFilters = [
        org.springframework.context.annotation.ComponentScan.Filter(
            type = org.springframework.context.annotation.FilterType.REGEX,
            pattern = [".*RedisRepository.*", ".*CacheRepository.*"]
        )
    ]
)
class KominioAiApplication

fun main(args: Array<String>) {
    runApplication<KominioAiApplication>(*args)
}