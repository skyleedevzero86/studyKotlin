package com.sleekydz86.youtubemsa.download.ledger

import com.sleekydz86.youtubemsa.download.persistence.DownloadEntity
import com.sleekydz86.youtubemsa.shared.DownloadEventType
import com.sleekydz86.youtubemsa.shared.DownloadStatus
import com.sleekydz86.youtubemsa.shared.HashingEmbedder
import com.sleekydz86.youtubemsa.shared.Reaction
import com.sleekydz86.youtubemsa.shared.SagaStep
import com.sleekydz86.youtubemsa.shared.sharedVideo
import org.junit.jupiter.api.DisplayName
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import com.sleekydz86.youtubemsa.shared.StudyException

@DisplayName("단위: 다운로드 원장")
class DownloadLedgerTest {
    private val rows = MemoryRows()
    private val outbox = MemorySaga()
    private val ledger = DownloadLedger(rows, HashingEmbedder(64), PassThroughLock(), outbox)

    @Test
    @DisplayName("같은 영상은 요청 중이면 새 행을 만들지 않는다")
    fun reusesRequested() {
        // given
        val video = sharedVideo("https://youtu.be/rjvqtbs9Q_g")
        val first = ledger.request(video)

        // when
        val second = ledger.request(video)

        // then
        assertEquals(first.id, second.id)
        assertEquals(DownloadStatus.REQUESTED.name, second.status)
        assertEquals(1, rows.all.size)
        assertEquals(1, outbox.events.size)
        assertEquals(DownloadEventType.DOWNLOAD_REQUESTED.name, outbox.events.single().type)
        assertEquals(SagaStep.REQUESTED.name, outbox.sagas.getValue(first.id).step)
    }

    @Test
    @DisplayName("요청 상태가 아니면 저장 시작을 거절한다")
    fun claimOnlyRequested() {
        // given
        val ticket = ledger.request(sharedVideo("https://youtu.be/rjvqtbs9Q_g"))
        rows.all.first().status = DownloadStatus.SAVED.name

        // when
        val claimed = ledger.claim(ticket.id, "event-1")

        // then
        assertNull(claimed)
    }

    @Test
    @DisplayName("저장이 끝난 행만 좋아요가 올라간다")
    fun likesSavedRow() {
        // given
        val ticket = ledger.request(sharedVideo("https://youtu.be/rjvqtbs9Q_g"))
        ledger.claim(ticket.id, "event-1")
        ledger.markSaved(ticket.id, com.sleekydz86.youtubemsa.shared.MarkSavedBody("b", "k", "등대 [rjvqtbs9Q_g].mp4", 10, "등대"))

        // when
        val liked = ledger.react(ticket.id, Reaction.LIKE)

        // then
        assertEquals(1, liked.likeCount)
        assertEquals(DownloadStatus.SAVED.name, liked.status)
    }

    @Test
    @DisplayName("실패 행은 좋아요를 받지 않는다")
    fun rejectsFailedLike() {
        // given
        val ticket = ledger.request(sharedVideo("https://youtu.be/rjvqtbs9Q_g"))
        ledger.claim(ticket.id, "event-1")
        ledger.markFailed(ticket.id, "yt-dlp 없음")

        // when
        val error = assertFailsWith<StudyException> { ledger.react(ticket.id, Reaction.LIKE) }

        // then
        assertEquals(409, error.status)
    }

    @Test
    @DisplayName("같은 이벤트는 저장 중 단계를 다시 열지 않는다")
    fun resumesSameClaim() {
        // given
        val ticket = ledger.request(sharedVideo("https://youtu.be/rjvqtbs9Q_g"))
        val first = ledger.claim(ticket.id, "event-1")

        // when
        val second = ledger.claim(ticket.id, "event-1")
        val other = ledger.claim(ticket.id, "event-2")

        // then
        assertEquals(first?.id, second?.id)
        assertNull(other)
        assertEquals(1, outbox.events.count { it.type == DownloadEventType.DOWNLOAD_CLAIMED.name })
        assertEquals(SagaStep.FULFILLING.name, outbox.sagas.getValue(ticket.id).step)
    }
}

private class MemoryRows : DownloadRows {
    val all = mutableListOf<DownloadEntity>()
    private var nextId = 1L

    override fun latest(videoId: String): DownloadEntity? = all.lastOrNull { it.videoId == videoId }

    override fun find(id: Long): DownloadEntity? = all.find { it.id == id }

    override fun save(entity: DownloadEntity): DownloadEntity {
        if (entity.id == 0L) {
            entity.id = nextId++
            if (entity.createdAt == Instant.EPOCH) entity.createdAt = Instant.parse("2026-10-14T00:00:00Z")
            all.add(entity)
            return entity
        }
        return entity
    }

    override fun markSaving(id: Long): Int {
        val entity = find(id) ?: return 0
        if (entity.status != DownloadStatus.REQUESTED.name) return 0
        entity.status = DownloadStatus.SAVING.name
        return 1
    }

    override fun saved(): List<DownloadEntity> = all.filter { it.status == DownloadStatus.SAVED.name }
}

private class PassThroughLock : ClaimLock {
    override fun <T> hold(id: Long, action: () -> T): T = action()
}

private class MemorySaga : SagaOutbox {
    val sagas = mutableMapOf<Long, SagaRecord>()
    val events = mutableListOf<OutboxRecord>()

    override fun saga(downloadId: Long): SagaRecord? = sagas[downloadId]

    override fun saveSaga(record: SagaRecord) {
        sagas[record.downloadId] = record
    }

    override fun append(event: OutboxRecord) {
        events.add(event)
    }
}
