package com.sleekydz86.youtubemsa.download.cdc

import com.sleekydz86.youtubemsa.download.DownloadProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Component
class ConnectorRegistrar(
    private val properties: DownloadProperties,
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(javaClass)
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
        val name = properties.cdc.connector
        if (exists(name)) {
            http.put().uri("/connectors/{name}/config", name).contentType(MediaType.APPLICATION_JSON).body(configJson()).retrieve().toBodilessEntity()
            log.info("Debezium 커넥터를 Outbox 라우팅으로 맞췄습니다. name={}", name)
            return
        }
        http.post().uri("/connectors").contentType(MediaType.APPLICATION_JSON).body(connectorJson()).retrieve().toBodilessEntity()
        log.info("Debezium Outbox 커넥터를 등록했습니다. name={}", name)
    }

    private fun exists(name: String): Boolean =
        try {
            http.get().uri("/connectors/{name}", name).retrieve().toBodilessEntity()
            true
        } catch (ex: RestClientResponseException) {
            if (ex.statusCode.value() == 404) false else throw ex
        }

    private fun connectorJson(): String =
        """{"name":"${properties.cdc.connector}","config":${configJson()}}"""

    private fun configJson(): String {
        val cdc = properties.cdc
        return """
            {
              "connector.class": "io.debezium.connector.mysql.MySqlConnector",
              "tasks.max": "1",
              "database.hostname": "${cdc.databaseHost}",
              "database.port": "${cdc.databasePort}",
              "database.user": "${cdc.databaseUser}",
              "database.password": "${cdc.databasePassword}",
              "database.server.id": "${cdc.serverId}",
              "database.ssl.mode": "disabled",
              "topic.prefix": "${cdc.topicPrefix}",
              "database.include.list": "${cdc.database}",
              "table.include.list": "${cdc.database}.outbox",
              "schema.history.internal.kafka.bootstrap.servers": "kafka:29092",
              "schema.history.internal.kafka.topic": "${cdc.topicPrefix}.schema-history",
              "include.schema.changes": "false",
              "snapshot.mode": "initial",
              "snapshot.locking.mode": "none",
              "decimal.handling.mode": "string",
              "tombstones.on.delete": "false",
              "transforms": "outbox",
              "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
              "transforms.outbox.table.field.event.id": "id",
              "transforms.outbox.table.field.event.key": "aggregateid",
              "transforms.outbox.table.field.event.payload": "payload",
              "transforms.outbox.route.by.field": "aggregatetype",
              "transforms.outbox.route.topic.replacement": "youtubemsa.download.events",
              "transforms.outbox.table.expand.json.payload": "true"
            }
        """.trimIndent()
    }
}
