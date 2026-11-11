package com.sleekydz86.cdcmigration.migration.infrastructure.kafka

import com.sleekydz86.cdcmigration.global.procedure.Worker
import com.sleekydz86.cdcmigration.global.procedure.WorkerContext
import com.sleekydz86.cdcmigration.migration.application.cdc.CdcIntakeService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class DebeziumEventListener(
    private val parser: DebeziumMessageParser,
    private val intake: CdcIntakeService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        id = "debezium-legacy",
        topicPattern = "\${app.cdc.topic-pattern}",
        autoStartup = "\${app.cdc.enabled:true}",
    )
    fun onMessage(record: ConsumerRecord<String?, String?>, acknowledgment: Acknowledgment) {
        val parsed = try {
            parser.parse(record.topic(), record.partition(), record.offset(), record.value())
        } catch (exception: Exception) {
            ParsedMessage.Ignored("읽을 수 없는 메시지: ${exception.message}")
        }
        val result = WorkerContext.runAs(Worker.CDC) {
            when (parsed) {
                is ParsedMessage.Change -> intake.receive(parsed.event, parsed.payload)
                is ParsedMessage.Notification -> intake.receive(parsed.notification)
                is ParsedMessage.Ignored -> parsed.reason
            }
        }
        log.debug("CDC 메시지 처리 토픽={} 오프셋={} 결과={}", record.topic(), record.offset(), result)
        acknowledgment.acknowledge()
    }
}
