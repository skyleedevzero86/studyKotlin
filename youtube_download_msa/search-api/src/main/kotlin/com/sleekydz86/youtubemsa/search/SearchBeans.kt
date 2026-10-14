package com.sleekydz86.youtubemsa.search

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import com.sleekydz86.youtubemsa.shared.HashingEmbedder
import jakarta.annotation.PreDestroy
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SearchBeans(properties: SearchProperties) {
    private val restClient = RestClient.builder(HttpHost.create(properties.elasticsearch.url)).build()
    private val transport = RestClientTransport(restClient, JacksonJsonpMapper())

    @Bean
    fun elasticsearchClient(): ElasticsearchClient = ElasticsearchClient(transport)

    @Bean
    fun hashingEmbedder(properties: SearchProperties): HashingEmbedder = HashingEmbedder(properties.elasticsearch.dims)

    @PreDestroy
    fun close() {
        transport.close()
    }
}
