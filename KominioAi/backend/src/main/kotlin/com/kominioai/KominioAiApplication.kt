package com.kominioai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableScheduling
@EnableR2dbcRepositories(
    basePackages = [
        "com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository"
    ]
)
@ComponentScan(basePackages = [
    "com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository",
    "com.kominioai.domain.survey.infrastructure.persistence.r2dbc.adapter"
])
class KominioAiApplication

fun main(args: Array<String>) {
    runApplication<KominioAiApplication>(*args)
}