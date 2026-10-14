package com.sleekydz86.youtubemsa.download.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "download_saga")
class DownloadSagaEntity(
    @Id
    @Column(name = "download_id")
    var downloadId: Long = 0,
    @Column(nullable = false, length = 20)
    var step: String = "",
    @Column(name = "event_id", nullable = false, length = 36)
    var eventId: String = "",
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH,
)
