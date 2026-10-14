package com.sleekydz86.youtubemsa.download

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(DownloadProperties::class)
class DownloadApplication

fun main(args: Array<String>) {
    runApplication<DownloadApplication>(*args)
}
