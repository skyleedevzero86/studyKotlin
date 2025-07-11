package com.kominioai.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableR2dbcRepositories(
    basePackages = [
        "com.kominioai.domain.survey.infrastructure.persistence.r2dbc.repository"
    ]
)
@EnableTransactionManagement
class DatabaseConfiguration