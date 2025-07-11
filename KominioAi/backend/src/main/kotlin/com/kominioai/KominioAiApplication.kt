package com.kominioai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(
    basePackages = [
        "com.kominioai.domain.survey.infrastructure.persistence.jpa.repository"
    ]
)
@ComponentScan(basePackages = [
    "com.kominioai.domain.survey.infrastructure.persistence.jpa.repository",
    "com.kominioai.domain.survey.infrastructure.persistence.jpa.adapter"
])
class KominioAiApplication

fun main(args: Array<String>) {
    runApplication<KominioAiApplication>(*args)
}