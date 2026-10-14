package com.sleekydz86.youtubemsa.download.ledger

import com.sleekydz86.youtubemsa.download.persistence.DownloadEntity
import com.sleekydz86.youtubemsa.shared.ClaimedView
import com.sleekydz86.youtubemsa.shared.DownloadEventType
import com.sleekydz86.youtubemsa.shared.DownloadStatus
import com.sleekydz86.youtubemsa.shared.DownloadTicket
import com.sleekydz86.youtubemsa.shared.DownloadView
import com.sleekydz86.youtubemsa.shared.HashingEmbedder
import com.sleekydz86.youtubemsa.shared.MarkSavedBody
import com.sleekydz86.youtubemsa.shared.Reaction
import com.sleekydz86.youtubemsa.shared.SagaStep
import com.sleekydz86.youtubemsa.shared.SharedVideo
import com.sleekydz86.youtubemsa.shared.StudyException
import com.sleekydz86.youtubemsa.shared.VectorCodec
import com.sleekydz86.youtubemsa.shared.downloadStatus
import com.sleekydz86.youtubemsa.shared.downloadText
import com.sleekydz86.youtubemsa.shared.eventPayload
import com.sleekydz86.youtubemsa.shared.nextSagaStep
import com.sleekydz86.youtubemsa.shared.reacted
import com.sleekydz86.youtubemsa.shared.requireEventId
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DownloadLedger(
    private val rows: DownloadRows,
    private val embedder: HashingEmbedder,
    private val lock: ClaimLock,
    private val outbox: SagaOutbox,
) {
    @Transactional
    fun request(video: SharedVideo): DownloadTicket {
        val existing = rows.latest(video.id.value)
        if (existing != null && existing.status == DownloadStatus.SAVED.name) return ticket(keepEmbedding(existing))
        if (existing != null && existing.status == DownloadStatus.REQUESTED.name) return ticket(keepEmbedding(existing))
        if (existing != null && existing.status == DownloadStatus.SAVING.name) {
            existing.status = DownloadStatus.REQUESTED.name
            existing.updatedAt = Instant.now()
            return ticket(publish(rows.save(withEmbedding(existing)), DownloadEventType.DOWNLOAD_REQUESTED))
        }
        return ticket(publish(rows.save(requested(video)), DownloadEventType.DOWNLOAD_REQUESTED))
    }

    @Transactional
    fun claim(id: Long, eventId: String): ClaimedView? {
        val deliveryId = requireEventId(eventId)
        return lock.hold(id) { claimLocked(id, deliveryId) }
    }

    @Transactional
    fun markSaved(id: Long, body: MarkSavedBody) {
        val entity = savingRow(id) ?: return
        entity.status = DownloadStatus.SAVED.name
        entity.title = body.title
        entity.bucket = body.bucket
        entity.objectKey = body.objectKey
        entity.fileName = body.fileName
        entity.sizeBytes = body.sizeBytes
        entity.errorMessage = null
        entity.updatedAt = Instant.now()
        entity.embeddingJson = encoded(entity)
        publish(rows.save(entity), DownloadEventType.DOWNLOAD_SAVED)
    }

    @Transactional
    fun markFailed(id: Long, message: String) {
        fail(id, message, DownloadEventType.DOWNLOAD_FAILED)
    }

    @Transactional
    fun markCompensated(id: Long, message: String) {
        fail(id, message, DownloadEventType.DOWNLOAD_COMPENSATED)
    }

    @Transactional(readOnly = true)
    fun find(id: Long): DownloadView? = rows.find(id)?.let(::viewOf)

    @Transactional(readOnly = true)
    fun saved(): List<DownloadView> = rows.saved().map(::viewOf)

    @Transactional
    fun react(id: Long, reaction: Reaction): DownloadView {
        val entity = rows.find(id)
            ?: throw StudyException("저장된 영상을 찾지 못했습니다. 번호: $id", status = 404)
        val next = reacted(viewOf(entity), reaction)
        entity.likeCount = next.likeCount
        entity.dislikeCount = next.dislikeCount
        entity.viewCount = next.viewCount
        entity.updatedAt = Instant.now()
        return viewOf(publish(rows.save(entity), DownloadEventType.DOWNLOAD_REACTED))
    }

    private fun claimLocked(id: Long, eventId: String): ClaimedView? {
        resumeClaim(id, eventId)?.let { return it }
        if (rows.markSaving(id) == 0) return null
        val entity = rows.find(id) ?: return null
        publish(entity, DownloadEventType.DOWNLOAD_CLAIMED, eventId, UUID.randomUUID().toString())
        return claimed(entity)
    }

    private fun resumeClaim(id: Long, eventId: String): ClaimedView? {
        val saga = outbox.saga(id) ?: return null
        if (saga.step != SagaStep.FULFILLING.name || saga.eventId != eventId) return null
        val entity = rows.find(id) ?: return null
        if (entity.status != DownloadStatus.SAVING.name) return null
        return claimed(entity)
    }

    private fun fail(id: Long, message: String, type: DownloadEventType) {
        val entity = savingRow(id) ?: return
        entity.status = DownloadStatus.FAILED.name
        entity.errorMessage = message
        entity.updatedAt = Instant.now()
        entity.embeddingJson = encoded(entity)
        publish(rows.save(entity), type)
    }

    private fun savingRow(id: Long): DownloadEntity? {
        val entity = rows.find(id) ?: return null
        if (entity.status != DownloadStatus.SAVING.name) return null
        return entity
    }

    private fun requested(video: SharedVideo): DownloadEntity {
        val now = Instant.now()
        return withEmbedding(
            DownloadEntity(
                videoId = video.id.value,
                watchUrl = video.watchUrl,
                status = DownloadStatus.REQUESTED.name,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun ticket(entity: DownloadEntity): DownloadTicket =
        DownloadTicket(entity.id, entity.videoId, statusName(entity))

    private fun claimed(entity: DownloadEntity): ClaimedView =
        ClaimedView(entity.id, entity.videoId, entity.watchUrl)

    private fun publish(entity: DownloadEntity, type: DownloadEventType): DownloadEntity {
        val eventId = UUID.randomUUID().toString()
        return publish(entity, type, eventId, eventId)
    }

    private fun publish(
        entity: DownloadEntity,
        type: DownloadEventType,
        sagaEventId: String,
        outboxId: String,
    ): DownloadEntity {
        val current = outbox.saga(entity.id)?.step?.let { step -> SagaStep.entries.firstOrNull { it.name == step } }
        val next = nextSagaStep(current, type)
        outbox.saveSaga(SagaRecord(entity.id, next.name, sagaEventId))
        outbox.append(
            OutboxRecord(
                id = outboxId,
                aggregateType = "Download",
                aggregateId = entity.id.toString(),
                type = type.name,
                payload = eventPayload(outboxId, type, entity.id, entity.status),
            ),
        )
        return entity
    }

    private fun keepEmbedding(entity: DownloadEntity): DownloadEntity {
        if (entity.embeddingJson.isNotBlank()) return entity
        return rows.save(withEmbedding(entity))
    }

    private fun withEmbedding(entity: DownloadEntity): DownloadEntity {
        if (entity.embeddingJson.isBlank()) entity.embeddingJson = encoded(entity)
        return entity
    }

    private fun encoded(entity: DownloadEntity): String =
        VectorCodec.encode(
            embedder.embed(downloadText(entity.videoId, entity.title, entity.fileName, entity.errorMessage)),
        )

    private fun statusName(entity: DownloadEntity): String =
        (downloadStatus(entity.status) ?: DownloadStatus.FAILED).name
}
