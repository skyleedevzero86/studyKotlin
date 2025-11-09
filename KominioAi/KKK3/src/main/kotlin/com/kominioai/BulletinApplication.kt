package com.kominioai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableR2dbcAuditing
@EnableAsync
class BulletinApplication

fun main(args: Array<String>) {
    runApplication<BulletinApplication>(*args)
}
