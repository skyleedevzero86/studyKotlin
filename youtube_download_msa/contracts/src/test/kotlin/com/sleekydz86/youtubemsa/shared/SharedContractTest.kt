package com.sleekydz86.youtubemsa.shared

import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("단위: 공유 계약")
class SharedContractTest {
    @Test
    @DisplayName("공유 주소에서 영상 ID만 남긴다")
    fun readsVideoId() {
        // given
        val raw = "https://www.youtube.com/live/rjvqtbs9Q_g?si=Iqw7I-Q4tg0R42Xe"

        // when
        val video = sharedVideo(raw)

        // then
        assertEquals("rjvqtbs9Q_g", video.id.value)
        assertEquals("https://www.youtube.com/watch?v=rjvqtbs9Q_g", video.watchUrl)
    }

    @Test
    @DisplayName("삭제 이벤트는 무시하고 생성 이벤트는 읽는다")
    fun parsesChange() {
        // given
        val created = """{"payload":{"op":"c","after":{"id":7,"status":"REQUESTED"}}}"""
        val deleted = """{"payload":{"op":"d","before":{"id":7,"status":"REQUESTED"}}}"""

        // when
        val change = downloadChange(created)

        // then
        assertEquals(7, change?.id)
        assertEquals(DownloadStatus.REQUESTED, change?.status)
        assertEquals(ChangeStep.FULFILL, changeStep(DownloadStatus.REQUESTED))
        assertNull(downloadChange(deleted))
    }

    @Test
    @DisplayName("실패 상태에는 좋아요를 남기지 않는다")
    fun rejectsLikeOnFailed() {
        // given
        val failed = sample().copy(status = DownloadStatus.FAILED.name)

        // when
        val error = assertFailsWith<StudyException> { reacted(failed, Reaction.LIKE) }

        // then
        assertTrue(error.message!!.contains("FAILED"))
        assertEquals(409, error.status)
    }

    @Test
    @DisplayName("좋아요가 많은 저장본을 빼고 상위 5개만 추천한다")
    fun recommendsFive() {
        // given
        val embedder = HashingEmbedder(64)
        val rows = (1..7).map { id ->
            val title = if (id == 7) "실패" else "바다 $id"
            val status = if (id == 7) DownloadStatus.FAILED.name else DownloadStatus.SAVED.name
            val vector = embedder.embed("$title aaaaabbbb0$id")
            sample().copy(
                id = id.toLong(),
                videoId = "aaaaabbbb0$id",
                title = title,
                status = status,
                embeddingJson = VectorCodec.encode(vector),
                likeCount = if (id == 1) 4 else 0,
            )
        }

        // when
        var requestedK = 0
        val page = recommendDownloads(
            anchorId = null,
            saved = rows,
            nearest = { vector, k ->
                requestedK = k
                rows.map { row -> VectorHit(row.id, CosineSimilarity.score(vector, storedVector(row, embedder))) }
                    .sortedByDescending { it.esScore }
                    .take(k)
            },
            find = { id -> rows.find { it.id == id } },
            fileUrl = { null },
            embedder = embedder,
            dims = 64,
            similarity = "cosine",
        )

        // then
        assertEquals(20, requestedK)
        assertEquals(recommendLimit, page.items.size)
        assertTrue(page.items.none { it.item.id == 1L || it.item.status == DownloadStatus.FAILED.name })
        assertEquals(page.items.map { it.cosine }.sortedDescending(), page.items.map { it.cosine })
    }

    @Test
    @DisplayName("9.5 미만 클러스터는 거절한다")
    fun versionGate() {
        // given
        val version = "8.15.3"

        // when
        ClusterVersionGate.assertLaterVectorStack("9.5.5")
        val error = assertFailsWith<StudyException> { ClusterVersionGate.assertLaterVectorStack(version) }

        // then
        assertTrue(error.message!!.contains(version))
    }

    private fun sample() = DownloadView(
        id = 4,
        videoId = "rjvqtbs9Q_g",
        watchUrl = "https://www.youtube.com/watch?v=rjvqtbs9Q_g",
        title = "등대",
        status = DownloadStatus.SAVED.name,
        updatedAt = "2026-10-14T08:00:00Z",
    )
}
