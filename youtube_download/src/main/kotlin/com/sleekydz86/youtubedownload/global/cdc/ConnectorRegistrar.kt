package com.sleekydz86.youtubedownload.global.cdc

import com.fasterxml.jackson.databind.ObjectMapper
import com.sleekydz86.youtubedownload.global.env.StudyProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class ConnectorRegistrar(
    private val properties: StudyProperties,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)
    private val json = ObjectMapper()
    private val http = RestClient.builder().baseUrl(properties.cdc.connectUrl).build()

    override fun run(args: ApplicationArguments) {
        repeat(10) { attempt ->
            try {
                registerOnce()
                return
            } catch (ex: Exception) {
                log.warn("Debezium 커넥터 등록 재시도 {}/10 ({})", attempt + 1, ex.javaClass.simpleName)
                Thread.sleep(3_000)
            }
        }
        log.warn("Debezium 커넥터를 등록하지 못했습니다. 목록 반영은 커넥터 등록 후에 됩니다.")
    }

    private fun registerOnce() {
        if (exists()) {
            log.info("Debezium 커넥터가 이미 있습니다. name={}", CONNECTOR)
            return
        }
        http.post()
            .uri("/connectors")
            .contentType(MediaType.APPLICATION_JSON)
            .body(connectorJson())
            .retrieve()
            .toBodilessEntity()
        log.info("Debezium 커넥터를 등록했습니다. name={}", CONNECTOR)
    }

    private fun exists(): Boolean =
        try {
            http.get().uri("/connectors/{name}", CONNECTOR).retrieve().toBodilessEntity()
            true
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.value() == 404) false else throw ex
        }

    private fun connectorJson(): String {
        val cdc = properties.cdc
        val config = mapOf(
            "connector.class" to "io.debezium.connector.mysql.MySqlConnector",
            "tasks.max" to "1",
            "database.hostname" to cdc.databaseHost,
            "database.port" to cdc.databasePort.toString(),
            "database.user" to cdc.databaseUser,
            "database.password" to cdc.databasePassword,
            "database.server.id" to cdc.serverId.toString(),
            "database.ssl.mode" to "disabled",
            "topic.prefix" to "youtube",
            "database.include.list" to "youtube_download",
            "table.include.list" to "youtube_download.download",
            "schema.history.internal.kafka.bootstrap.servers" to "kafka:29092",
            "schema.history.internal.kafka.topic" to "youtube.schema-history",
            "include.schema.changes" to "false",
            "snapshot.mode" to "initial",
            "snapshot.locking.mode" to "none",
            "decimal.handling.mode" to "string",
        )
        return json.writeValueAsString(mapOf("name" to CONNECTOR, "config" to config))
    }

    companion object {
        private const val CONNECTOR = "youtube-download"
    }
}
