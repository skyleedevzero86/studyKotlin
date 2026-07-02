package com.kochat.adapter.outbound.persistence.messaging

import com.kochat.domain.messaging.model.DlqEventStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "dlq_events",
    indexes = [
        Index(name = "idx_dlq_topic_received", columnList = "source_topic, received_at"),
        Index(name = "idx_dlq_status", columnList = "status"),
    ],
)
class DlqEventJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "source_topic", nullable = false, length = 120)
    var sourceTopic: String = ""

    @Column(name = "partition_num", nullable = false)
    var partitionNum: Int = 0

    @Column(name = "record_offset", nullable = false)
    var recordOffset: Long = 0

    @Column(name = "record_key", length = 64)
    var recordKey: String? = null

    @Column(nullable = false, columnDefinition = "TEXT")
    var payload: String = ""

    @Column(name = "error_message", length = 2000)
    var errorMessage: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: DlqEventStatus = DlqEventStatus.OPEN

    @Column(name = "received_at", nullable = false)
    var receivedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "replayed_at")
    var replayedAt: LocalDateTime? = null
}
