package com.sleekydz86.densevector2.global.config

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import jakarta.annotation.PreDestroy

@Configuration
class ElasticsearchConfig(properties: VectorProperties) {
    private val restClient = RestClient.builder(HttpHost.create(properties.elasticsearchUrl)).build()
    private val transport = RestClientTransport(restClient, JacksonJsonpMapper())

    @Bean
    fun elasticsearchClient(): ElasticsearchClient = ElasticsearchClient(transport)

    @PreDestroy
    fun close() {
        transport.close()
    }
}
