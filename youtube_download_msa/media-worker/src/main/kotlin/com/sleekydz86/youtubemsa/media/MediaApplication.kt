package com.sleekydz86.youtubemsa.media

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(MediaProperties::class)
class MediaApplication

fun main(args: Array<String>) {
    runApplication<MediaApplication>(*args)
}
