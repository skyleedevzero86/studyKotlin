package com.sleekydz86.youtubemsa.download.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "outbox")
class OutboxEntity(
    @Id
    @Column(length = 36)
    var id: String = "",
    @Column(name = "aggregatetype", nullable = false, length = 40)
    var aggregateType: String = "",
    @Column(name = "aggregateid", nullable = false, length = 40)
    var aggregateId: String = "",
    @Column(nullable = false, length = 40)
    var type: String = "",
    @Column(nullable = false, length = 1000)
    var payload: String = "",
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.EPOCH,
)
