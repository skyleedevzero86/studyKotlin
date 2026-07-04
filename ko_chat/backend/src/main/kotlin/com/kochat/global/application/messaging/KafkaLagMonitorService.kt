package com.kochat.global.application.messaging

import com.kochat.global.application.messaging.dto.ConsumerLagSnapshot
import com.kochat.global.application.messaging.dto.KafkaLagReport
import com.kochat.global.config.KafkaProperties
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsSpec
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime
import java.util.Properties

@Service
@ConditionalOnProperty(prefix = "app.kafka", name = ["enabled"], havingValue = "true")
class KafkaLagMonitorService(
    private val kafkaProperties: KafkaProperties,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Volatile
    private var lastReport: KafkaLagReport = KafkaLagReport(emptyList(), true, 0, LocalDateTime.now())

    fun collectLag(): KafkaLagReport {
        if (!kafkaProperties.enabled) {
            return KafkaLagReport(emptyList(), true, 0, LocalDateTime.now())
        }

        val props = Properties().apply {
            put("bootstrap.servers", kafkaProperties.bootstrapServers)
        }

        return try {
            AdminClient.create(props).use { admin ->
                val groups = listOf(
                    kafkaProperties.consumerGroups.audit,
                    kafkaProperties.consumerGroups.searchIndex,
                    kafkaProperties.consumerGroups.attachment,
                    kafkaProperties.consumerGroups.milvus,
                )

                val snapshots = mutableListOf<ConsumerLagSnapshot>()
                groups.forEach { groupId ->
                    val offsets: Map<TopicPartition, OffsetAndMetadata> = admin.listConsumerGroupOffsets(
                        mapOf(groupId to ListConsumerGroupOffsetsSpec()),
                    ).all().get()[groupId] ?: emptyMap()

                    if (offsets.isEmpty()) {
                        return@forEach
                    }

                    val endOffsets = admin.listOffsets(
                        offsets.keys.associateWith { org.apache.kafka.clients.admin.OffsetSpec.latest() },
                    ).all().get()

                    offsets.forEach { (partition, committed) ->
                        val end = endOffsets[partition]?.offset() ?: committed.offset()
                        val lag = (end - committed.offset()).coerceAtLeast(0)
                        snapshots.add(
                            ConsumerLagSnapshot(
                                consumerGroup = groupId,
                                topic = partition.topic(),
                                partition = partition.partition(),
                                currentOffset = committed.offset(),
                                endOffset = end,
                                lag = lag,
                            ),
                        )
                    }
                }

                val maxLag = snapshots.maxOfOrNull { it.lag } ?: 0
                val healthy = maxLag < kafkaProperties.lagCriticalThreshold
                val report = KafkaLagReport(
                    snapshots = snapshots.sortedByDescending { it.lag },
                    healthy = healthy,
                    maxLag = maxLag,
                    checkedAt = LocalDateTime.now(),
                )
                lastReport = report
                if (maxLag >= kafkaProperties.lagWarningThreshold) {
                    logger.warn("Kafka consumer lag 경고: maxLag={}, groups={}", maxLag, groups)
                }
                report
            }
        } catch (ex: Exception) {
            logger.warn("Kafka lag 수집 실패: {}", ex.message)
            KafkaLagReport(emptyList(), false, -1, LocalDateTime.now())
        }
    }

    fun getLastReport(): KafkaLagReport = lastReport

    @Scheduled(fixedDelayString = "\${app.kafka.lag-monitor-interval-ms:30000}")
    fun scheduledLagCheck() {
        collectLag()
    }
}
