package com.sleekydz86.youtubemsa.download.ledger

import com.sleekydz86.youtubemsa.download.persistence.DownloadSagaEntity
import com.sleekydz86.youtubemsa.download.persistence.DownloadSagaRepository
import com.sleekydz86.youtubemsa.download.persistence.OutboxEntity
import com.sleekydz86.youtubemsa.download.persistence.OutboxRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SpringSagaOutbox(
    private val outbox: OutboxRepository,
    private val sagas: DownloadSagaRepository,
) : SagaOutbox {
    override fun saga(downloadId: Long): SagaRecord? =
        sagas.findById(downloadId).map { SagaRecord(it.downloadId, it.step, it.eventId) }.orElse(null)

    override fun saveSaga(record: SagaRecord) {
        val entity = sagas.findById(record.downloadId).orElse(DownloadSagaEntity(downloadId = record.downloadId))
        entity.step = record.step
        entity.eventId = record.eventId
        entity.updatedAt = Instant.now()
        sagas.save(entity)
    }

    override fun append(event: OutboxRecord) {
        outbox.save(
            OutboxEntity(
                id = event.id,
                aggregateType = event.aggregateType,
                aggregateId = event.aggregateId,
                type = event.type,
                payload = event.payload,
                createdAt = Instant.now(),
            ),
        )
    }
}
