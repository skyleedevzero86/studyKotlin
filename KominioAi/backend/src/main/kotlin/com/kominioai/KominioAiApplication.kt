package com.kominioai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication
@EnableScheduling
@EnableR2dbcRepositories(
    basePackages = [
        "com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository"
    ]
)
@EnableRedisRepositories(
    basePackages = [
        "com.kominioai.domain.survey.infrastructure.persistence.redis.repository"
    ]
)
class KominioAiApplication

fun main(args: Array<String>) {
    runApplication<KominioAiApplication>(*args)
}