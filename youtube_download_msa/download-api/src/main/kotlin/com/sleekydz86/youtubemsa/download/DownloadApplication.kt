package com.sleekydz86.youtubemsa.download

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(DownloadProperties::class)
class DownloadApplication

fun main(args: Array<String>) {
    runApplication<DownloadApplication>(*args)
}
