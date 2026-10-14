package com.sleekydz86.youtubedownload.unit.video

import com.sleekydz86.youtubedownload.global.embedding.DenseVectorMapping
import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.read.recommendLimit
import com.sleekydz86.youtubedownload.video.read.recommendationAnchor
import com.sleekydz86.youtubedownload.video.read.savedQuery
import com.sleekydz86.youtubedownload.video.read.wildcardPattern
import com.sleekydz86.youtubedownload.video.react.Reaction
import com.sleekydz86.youtubedownload.video.react.reacted
import org.junit.jupiter.api.DisplayName
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("단위: 반응, 검색어, 추천 기준")
class ReactionRulesTest {
    @Test
    @DisplayName("저장된 영상의 좋아요만 1 증가한다")
    fun likeIncrementsSavedRow() {
        // given
        val saved = sample(DownloadStatus.SAVED, likeCount = 1)

        // when
        val next = reacted(saved, Reaction.LIKE)

        // then
        assertEquals(2, next.likeCount)
        assertEquals(0, next.dislikeCount)
        assertEquals(0, next.viewCount)
    }

    @Test
    @DisplayName("실패 상태에는 조회를 남기지 않는다")
    fun viewRejectsFailedRow() {
        // given
        val failed = sample(DownloadStatus.FAILED)

        // when
        val error = assertFailsWith<DownloadException> { reacted(failed, Reaction.VIEW) }

        // then
        assertTrue(error.message!!.contains("FAILED"))
        assertTrue(error.message!!.contains("4"))
    }

    @Test
    @DisplayName("빈 검색어와 81자는 거절하고 와일드카드는 이스케이프한다")
    fun queryRules() {
        // given
        val longText = "a".repeat(81)

        // when
        val blank = assertFailsWith<DownloadException> { savedQuery("  ") }
        val tooLong = assertFailsWith<DownloadException> { savedQuery(longText) }
        val pattern = wildcardPattern("a*?\\b")

        // then
        assertTrue(blank.message!!.contains("비어"))
        assertTrue(tooLong.message!!.contains(longText))
        assertEquals("""*a\*\?\\b*""", pattern)
        assertEquals("등대", savedQuery(" 등대 "))
    }

    @Test
    @DisplayName("추천 기준은 좋아요, 조회, 최신 순이고 저장본이 없으면 없다")
    fun anchorPrefersLikes() {
        // given
        val older = sample(DownloadStatus.SAVED, id = 1, likeCount = 1, viewCount = 9, at = "2026-10-14T01:00:00Z")
        val liked = sample(DownloadStatus.SAVED, id = 2, likeCount = 3, at = "2026-10-14T02:00:00Z")
        val failed = sample(DownloadStatus.FAILED, id = 3, likeCount = 9)

        // when
        val anchor = recommendationAnchor(listOf(older, liked, failed), null)
        val missing = recommendationAnchor(listOf(failed), null)
        val byId = recommendationAnchor(listOf(older, liked), 1)

        // then
        assertEquals(2, anchor?.id)
        assertNull(missing)
        assertEquals(1, byId?.id)
        assertEquals(5, recommendLimit)
        val absent = assertFailsWith<DownloadException> { recommendationAnchor(listOf(older), 9) }
        assertTrue(absent.message!!.contains("9"))
    }

    @Test
    @DisplayName("인덱스 매핑에 반응 횟수 필드가 있다")
    fun mappingHasReactionFields() {
        // given
        val json = DenseVectorMapping.createIndexJson(64, "cosine", "bbq_hnsw", 16, 100)

        // when
        val fields = DenseVectorMapping.reactionFieldsJson()

        // then
        assertTrue(json.contains("likeCount"))
        assertTrue(fields.contains("dislikeCount"))
        assertTrue(fields.contains("viewCount"))
    }

    private fun sample(
        status: DownloadStatus,
        id: Long = 4,
        likeCount: Int = 0,
        viewCount: Int = 0,
        at: String = "2026-10-14T08:00:00Z",
    ) = DownloadSnapshot(
        id = id,
        videoId = VideoId("rjvqtbs9Q_g"),
        title = "등대",
        status = status,
        bucket = "youtube-download",
        objectKey = "a.mp4",
        fileName = "a.mp4",
        sizeBytes = 12,
        errorMessage = null,
        updatedAt = Instant.parse(at),
        likeCount = likeCount,
        viewCount = viewCount,
    )
}
