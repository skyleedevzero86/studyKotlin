package com.sleekydz86.cdcmigration.migration.infrastructure.debezium

import com.sleekydz86.cdcmigration.global.config.AppProperties
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import com.sleekydz86.cdcmigration.migration.application.ConnectorClient
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import tools.jackson.databind.json.JsonMapper

@Component
class DebeziumConnectClient(private val properties: AppProperties) : ConnectorClient {

    private val json = JsonMapper.builder().build()
    private val client = RestClient.create(properties.cdc.connectUrl)

    override fun register(name: String, config: Map<String, String>) {
        call("커넥터 $name 등록") {
            client.put().uri("/connectors/{name}/config", name)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json.writeValueAsString(config))
                .retrieve()
                .toBodilessEntity()
        }
    }

    override fun delete(name: String) {
        try {
            call("커넥터 $name 삭제") {
                client.delete().uri("/connectors/{name}", name).retrieve().toBodilessEntity()
            }
        } catch (exception: BusinessException) {
            if (exception.cause !is RestClientResponseException ||
                (exception.cause as RestClientResponseException).statusCode != HttpStatus.NOT_FOUND
            ) {
                throw exception
            }
        }
    }

    override fun state(name: String): String? {
        val body = call("커넥터 $name 상태 조회") {
            client.get().uri("/connectors/{name}/status", name).retrieve().body(String::class.java)
        } ?: return null
        val root = json.readTree(body)
        val connector = root.path("connector").path("state").asString("UNKNOWN")
        val task = root.path("tasks").path(0).path("state").asString("-")
        return "커넥터 $connector / 태스크 $task"
    }

    private fun <T> call(action: String, block: () -> T): T = try {
        block()
    } catch (exception: RestClientResponseException) {
        throw BusinessException(
            ErrorCode.CONNECT_FAILED,
            "$action 실패 (HTTP ${exception.statusCode.value()}): ${exception.responseBodyAsString.take(300)}",
            exception,
        )
    } catch (exception: ResourceAccessException) {
        throw BusinessException(
            ErrorCode.CONNECT_FAILED,
            "$action 실패: Debezium Connect(${properties.cdc.connectUrl})에 연결할 수 없습니다. docker compose로 connect 서비스를 띄웠는지 확인하세요.",
            exception,
        )
    }
}
