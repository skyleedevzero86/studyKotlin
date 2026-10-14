package com.sleekydz86.youtubedownload.feature

import com.sleekydz86.youtubedownload.global.embedding.CosineSimilarity
import com.sleekydz86.youtubedownload.global.embedding.HashingEmbedder
import com.sleekydz86.youtubedownload.global.embedding.VectorCodec
import com.sleekydz86.youtubedownload.support.MemoryLinks
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.read.DownloadTextSearch
import com.sleekydz86.youtubedownload.video.read.DownloadVectorSearch
import com.sleekydz86.youtubedownload.video.read.IndexedDownload
import com.sleekydz86.youtubedownload.video.read.IndexedPage
import com.sleekydz86.youtubedownload.video.read.PageQuery
import com.sleekydz86.youtubedownload.video.read.SavedCatalog
import com.sleekydz86.youtubedownload.video.read.VectorHit
import com.sleekydz86.youtubedownload.video.read.recommendDownloads
import com.sleekydz86.youtubedownload.video.read.recommendLimit
import com.sleekydz86.youtubedownload.video.read.searchSavedDownloads
import com.sleekydz86.youtubedownload.video.read.storedVector
import com.sleekydz86.youtubedownload.video.react.DownloadReactions
import com.sleekydz86.youtubedownload.video.react.Reaction
import com.sleekydz86.youtubedownload.video.react.reacted
import org.junit.jupiter.api.DisplayName
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("종합: 검색, 반응, 벡터 추천")
class RecommendationFlowTest {
    private val embedder = HashingEmbedder(64)

    @Test
    @DisplayName("좋아요가 많은 영상을 기준으로 상위 5개를 추천한다")
    fun recommendsFiveFromLikedAnchor() {
        // given
        val catalog = MemoryCatalog(
            listOf(
                saved(1, "aaaaabbbbb1", "바다 등대", likes = 4),
                saved(2, "aaaaabbbbb2", "바다 항구"),
                saved(3, "aaaaabbbbb3", "바다 선착장"),
                saved(4, "aaaaabbbbb4", "산 등반"),
                saved(5, "aaaaabbbbb5", "도시 야경"),
                saved(6, "aaaaabbbbb6", "사막 여행"),
                saved(7, "aaaaabbbbb7", "실패 영상", status = DownloadStatus.FAILED),
            ),
        )
        val nearest = CosineNearest(catalog.rows, embedder)

        // when
        val found = searchSavedDownloads(TitleSearch(catalog.rows), MemoryLinks(), "등대", 0, 10)
        val liked = catalog.react(found.items.single().id, Reaction.LIKE)
        val page = recommendDownloads(null, catalog, nearest, catalog::find, MemoryLinks(), embedder, 64, "cosine")

        // then
        assertEquals(5, liked.likeCount)
        assertEquals(recommendLimit, page.items.size)
        assertTrue(page.items.none { it.item.id == 1L || it.item.status == "FAILED" })
        val cosines = page.items.map { it.cosine }
        assertEquals(cosines.sortedDescending(), cosines)
    }

    private fun saved(
        id: Long,
        videoId: String,
        title: String,
        likes: Int = 0,
        status: DownloadStatus = DownloadStatus.SAVED,
    ): DownloadSnapshot {
        val vector = embedder.embed("$title $videoId")
        return DownloadSnapshot(
            id = id,
            videoId = VideoId(videoId),
            title = title,
            status = status,
            bucket = "youtube-download",
            objectKey = "a.mp4",
            fileName = "$title.mp4",
            sizeBytes = 12,
            errorMessage = null,
            updatedAt = Instant.parse("2026-10-14T08:00:00Z").plusSeconds(id),
            embeddingJson = VectorCodec.encode(vector),
            likeCount = likes,
        )
    }
}

private class MemoryCatalog(rows: List<DownloadSnapshot>) : SavedCatalog, DownloadReactions {
    var rows: List<DownloadSnapshot> = rows

    override fun saved(): List<DownloadSnapshot> = rows.filter { it.status == DownloadStatus.SAVED }

    fun find(id: Long): DownloadSnapshot? = rows.find { it.id == id }

    override fun react(id: Long, reaction: Reaction): DownloadSnapshot {
        val current = find(id) ?: error("없음")
        val next = reacted(current, reaction)
        rows = rows.map { if (it.id == id) next else it }
        return next
    }
}

private class CosineNearest(
    private val rows: List<DownloadSnapshot>,
    private val embedder: HashingEmbedder,
) : DownloadVectorSearch {
    override fun nearest(vector: FloatArray, k: Int): List<VectorHit> =
        rows.map { row -> VectorHit(row.id, CosineSimilarity.score(vector, storedVector(row, embedder))) }
            .sortedByDescending { it.esScore }
            .take(k)
}

private class TitleSearch(private val rows: List<DownloadSnapshot>) : DownloadTextSearch {
    override fun searchSaved(text: String, query: PageQuery): IndexedPage {
        val matched = rows.filter { it.status == DownloadStatus.SAVED && it.title?.contains(text) == true }
        val items = matched.map { row ->
            IndexedDownload(
                id = row.id,
                videoId = row.videoId.value,
                title = row.title,
                status = row.status,
                bucket = row.bucket,
                objectKey = row.objectKey,
                fileName = row.fileName,
                sizeBytes = row.sizeBytes,
                errorMessage = row.errorMessage,
                updatedAt = row.updatedAt.toString(),
                likeCount = row.likeCount,
            )
        }
        return IndexedPage(query.page, query.size, items.size.toLong(), items)
    }
}
