package com.sleekydz86.youtubemsa.search

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(SearchProperties::class)
class SearchApplication

fun main(args: Array<String>) {
    runApplication<SearchApplication>(*args)
}
