package com.sleekydz86.youtubedownload.unit.video

import com.sleekydz86.youtubedownload.global.cdc.ChangeStep
import com.sleekydz86.youtubedownload.global.cdc.changeStep
import com.sleekydz86.youtubedownload.global.cdc.downloadChange
import com.sleekydz86.youtubedownload.global.cdc.fulfillDownload
import com.sleekydz86.youtubedownload.global.cdc.projectDownload
import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.global.minio.mediaType
import com.sleekydz86.youtubedownload.support.FailingVideoFiles
import com.sleekydz86.youtubedownload.support.HoldingLock
import com.sleekydz86.youtubedownload.support.MemoryClaims
import com.sleekydz86.youtubedownload.support.MemoryLinks
import com.sleekydz86.youtubedownload.support.MemoryProjection
import com.sleekydz86.youtubedownload.support.MemoryRemoteStore
import com.sleekydz86.youtubedownload.support.MemoryRequests
import com.sleekydz86.youtubedownload.support.MemorySearch
import com.sleekydz86.youtubedownload.support.RecordingVideoFiles
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.read.IndexedDownload
import com.sleekydz86.youtubedownload.video.read.IndexedPage
import com.sleekydz86.youtubedownload.video.read.listDownloads
import com.sleekydz86.youtubedownload.video.read.pageQuery
import com.sleekydz86.youtubedownload.video.save.ClaimedDownload
import com.sleekydz86.youtubedownload.video.save.requestDownload
import com.sleekydz86.youtubedownload.video.titleFromFile
import java.nio.file.Path
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DownloadFlowTest {
    @Test
    fun pasteKeepsCanonicalVideo() {
        val requests = MemoryRequests()
        val ticket = requestDownload(requests, "https://www.youtube.com/live/rjvqtbs9Q_g?si=Iqw7I-Q4tg0R42Xe")
        assertEquals("rjvqtbs9Q_g", requests.video?.id?.value)
        assertEquals("https://www.youtube.com/watch?v=rjvqtbs9Q_g", requests.video?.watchUrl)
        assertEquals(DownloadStatus.REQUESTED, ticket.status)
    }

    @Test
    fun pageQueryRejectsOutOfRange() {
        val page = assertFailsWith<DownloadException> { pageQuery(-1, 10) }
        val size = assertFailsWith<DownloadException> { pageQuery(0, 51) }
        assertTrue(page.message!!.contains("-1"))
        assertTrue(size.message!!.contains("51"))
        assertEquals(10, pageQuery(null, null).size)
    }

    @Test
    fun listLinksOnlySavedFiles() {
        val page = IndexedPage(
            page = 0,
            size = 10,
            total = 2,
            items = listOf(savedItem(), requestedItem()),
        )
        val listed = listDownloads(MemorySearch(page), MemoryLinks(), pageQuery(0, 10))
        assertEquals("http://127.0.0.1:9000/youtube-download/a.mp4", listed.items[0].fileUrl)
        assertNull(listed.items[1].fileUrl)
        assertEquals(2, listed.total)
    }

    @Test
    fun changeParserReadsDebeziumEnvelope() {
        val message = """{"payload":{"op":"c","after":{"id":7,"status":"REQUESTED"}}}"""
        val change = downloadChange(message)
        assertEquals(7, change?.id)
        assertEquals(DownloadStatus.REQUESTED, change?.status)
        assertNull(downloadChange("""{"payload":{"op":"d","before":{"id":7}}}"""))
        assertNull(downloadChange("not-json"))
        assertEquals(ChangeStep.FULFILL, changeStep(DownloadStatus.REQUESTED))
        assertEquals(ChangeStep.PROJECT, changeStep(DownloadStatus.SAVED))
    }

    @Test
    fun fulfillUploadsClaimedFile() {
        val claims = MemoryClaims()
        val remote = MemoryRemoteStore()
        val files = RecordingVideoFiles()
        claims.claimed = ClaimedDownload(4, SharedVideo(VideoId("rjvqtbs9Q_g")))
        fulfillDownload(4, claims, files, remote, HoldingLock(), Path.of("work"))
        assertEquals(1, remote.puts)
        assertEquals("saved", claims.savedTitle)
        assertEquals("saved.mp4", claims.savedFile?.fileName)
    }

    @Test
    fun fulfillSkipsWhenClaimIsGone() {
        val remote = MemoryRemoteStore()
        fulfillDownload(4, MemoryClaims(), RecordingVideoFiles(), remote, HoldingLock(), Path.of("work"))
        assertEquals(0, remote.puts)
    }

    @Test
    fun fulfillFailureMarksRow() {
        val claims = MemoryClaims()
        claims.claimed = ClaimedDownload(4, SharedVideo(VideoId("rjvqtbs9Q_g")))
        fulfillDownload(4, claims, FailingVideoFiles(), MemoryRemoteStore(), HoldingLock(), Path.of("work"))
        assertTrue(claims.failed!!.contains("실패"))
    }

    @Test
    fun projectWritesSnapshot() {
        val claims = MemoryClaims()
        val projection = MemoryProjection()
        claims.snapshot = DownloadSnapshot(
            id = 4,
            videoId = VideoId("rjvqtbs9Q_g"),
            title = "라이브",
            status = DownloadStatus.SAVED,
            bucket = "youtube-download",
            objectKey = "a.mp4",
            fileName = "a.mp4",
            sizeBytes = 12,
            errorMessage = null,
            updatedAt = Instant.parse("2026-10-14T08:00:00Z"),
        )
        projectDownload(4, claims, projection)
        assertEquals(listOf(4L), projection.saved)
    }

    @Test
    fun titleStripsVideoId() {
        assertEquals("라이브", titleFromFile("라이브 [rjvqtbs9Q_g].mp4", VideoId("rjvqtbs9Q_g")))
        assertEquals("video/mp4", mediaType("clip.mp4"))
        assertEquals("application/octet-stream", mediaType("clip.bin"))
    }

    private fun savedItem(): IndexedDownload = IndexedDownload(
        id = 1,
        videoId = "rjvqtbs9Q_g",
        title = "라이브",
        status = DownloadStatus.SAVED,
        bucket = "youtube-download",
        objectKey = "a.mp4",
        fileName = "a.mp4",
        sizeBytes = 12,
        errorMessage = null,
        updatedAt = "2026-10-14T08:00:00Z",
    )

    private fun requestedItem(): IndexedDownload = savedItem().copy(
        id = 2,
        status = DownloadStatus.REQUESTED,
        bucket = null,
        objectKey = null,
    )
}
