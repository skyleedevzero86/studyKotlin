package com.kominioai.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories(
    basePackages = [
        "com.kominioai.domain.survey.adapter.out.persistence",
        "com.kominioai.domain.survey.application.port.out.persistence"
    ],
    excludeFilters = [
        org.springframework.context.annotation.ComponentScan.Filter(
            type = org.springframework.context.annotation.FilterType.REGEX,
            pattern = [".*Redis.*", ".*Cache.*"]
        )
    ]
)
class DatabaseConfiguration