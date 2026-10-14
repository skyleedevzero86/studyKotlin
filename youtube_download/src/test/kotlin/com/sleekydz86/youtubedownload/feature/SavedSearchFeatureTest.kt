package com.sleekydz86.youtubedownload.feature

import com.sleekydz86.youtubedownload.support.MemoryLinks
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.read.DownloadTextSearch
import com.sleekydz86.youtubedownload.video.read.IndexedDownload
import com.sleekydz86.youtubedownload.video.read.IndexedPage
import com.sleekydz86.youtubedownload.video.read.PageQuery
import com.sleekydz86.youtubedownload.video.read.searchSavedDownloads
import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("기능: 저장된 영상 검색")
class SavedSearchFeatureTest {
    @Test
    @DisplayName("제목에 검색어가 있는 저장본만 돌려준다")
    fun findsSavedTitle() {
        // given
        val search = FixedTextSearch(
            listOf(
                row(1, DownloadStatus.SAVED, "바다 등대"),
                row(2, DownloadStatus.SAVED, "산 등반"),
                row(3, DownloadStatus.FAILED, "바다 실패"),
            ),
        )

        // when
        val page = searchSavedDownloads(search, MemoryLinks(), " 등대 ", 0, 10)

        // then
        assertEquals(listOf(1L), page.items.map { it.id })
        assertEquals("바다 등대", page.items.single().title)
        assertEquals("http://127.0.0.1:9000/youtube-download/a.mp4", page.items.single().fileUrl)
        assertTrue(search.texts.single() == "등대")
    }

    private fun row(id: Long, status: DownloadStatus, title: String) = IndexedDownload(
        id = id,
        videoId = "rjvqtbs9Q_g",
        title = title,
        status = status,
        bucket = "youtube-download",
        objectKey = "a.mp4",
        fileName = "a.mp4",
        sizeBytes = 12,
        errorMessage = null,
        updatedAt = "2026-10-14T08:00:00Z",
    )
}

private class FixedTextSearch(private val rows: List<IndexedDownload>) : DownloadTextSearch {
    val texts = mutableListOf<String>()

    override fun searchSaved(text: String, query: PageQuery): IndexedPage {
        texts += text
        val matched = rows.filter { row ->
            row.status == DownloadStatus.SAVED && (row.title?.contains(text) == true || row.videoId.contains(text))
        }
        return IndexedPage(query.page, query.size, matched.size.toLong(), matched)
    }
}
