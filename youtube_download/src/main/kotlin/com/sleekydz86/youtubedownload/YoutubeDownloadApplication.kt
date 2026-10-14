package com.sleekydz86.youtubedownload

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class YoutubeDownloadApplication

fun main(args: Array<String>) {
    runApplication<YoutubeDownloadApplication>(*args)
}
