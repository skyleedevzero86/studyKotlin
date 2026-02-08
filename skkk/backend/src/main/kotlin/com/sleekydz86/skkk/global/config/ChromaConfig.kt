package com.sleekydz86.skkk.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ChromaConfig {

    @Value("\${chroma.host}")
    private lateinit var chromaHost: String

    @Bean
    fun chromaWebClient(): WebClient = WebClient.builder()
        .baseUrl("$chromaHost/api/v2")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()

    @Value("\${embedding.host}")
    private lateinit var embeddingHost: String

    @Bean
    fun embeddingWebClient(): WebClient = WebClient.builder()
        .baseUrl(embeddingHost)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()
}
