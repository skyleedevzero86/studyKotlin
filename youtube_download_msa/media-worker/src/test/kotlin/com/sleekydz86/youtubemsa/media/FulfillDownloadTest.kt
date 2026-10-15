package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.ClaimedView
import com.sleekydz86.youtubemsa.shared.DeliveryInbox
import com.sleekydz86.youtubemsa.shared.MarkSavedBody
import com.sleekydz86.youtubemsa.shared.SharedVideo
import com.sleekydz86.youtubemsa.shared.StudyException
import com.sleekydz86.youtubemsa.shared.VideoId
import com.sleekydz86.youtubemsa.shared.acceptDelivery
import org.junit.jupiter.api.DisplayName
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("기능: 미디어 저장")
class FulfillDownloadTest {
    @Test
    @DisplayName("요청을 차지하면 파일을 올리고 저장 완료를 남긴다")
    fun savesClaimedVideo() {
        // given
        val ledger = RecordingLedger(ClaimedView(4, "rjvqtbs9Q_g", "https://www.youtube.com/watch?v=rjvqtbs9Q_g"))
        val root = Files.createTempDirectory("msa-fulfill")
        val files = FixedVideoFile()
        val remote = FixedRemoteStore()

        // when
        fulfillDownload(4, "event-1", ledger, files, remote, root)

        // then
        assertEquals("등대", ledger.saved?.title)
        assertEquals("rjvqtbs9Q_g/4/등대 [rjvqtbs9Q_g].mp4", ledger.saved?.objectKey)
        assertTrue(Files.notExists(root.resolve("4")))
    }

    @Test
    @DisplayName("파일 저장이 실패하면 실패 상태를 남긴다")
    fun marksFailure() {
        // given
        val ledger = RecordingLedger(ClaimedView(4, "rjvqtbs9Q_g", "https://www.youtube.com/watch?v=rjvqtbs9Q_g"))
        val files = FailingVideoFile()

        // when
        fulfillDownload(4, "event-1", ledger, files, UnusedRemoteStore(), Path.of("unused"))

        // then
        assertTrue(ledger.failed.orEmpty().contains("yt-dlp"))
        assertEquals(null, ledger.saved)
    }

    @Test
    @DisplayName("파일을 올린 뒤 원장 반영이 실패하면 객체를 지우고 보상한다")
    fun compensatesUpload() {
        // given
        val ledger = SaveFailingLedger(ClaimedView(4, "rjvqtbs9Q_g", "https://www.youtube.com/watch?v=rjvqtbs9Q_g"))
        val remote = FixedRemoteStore()
        val root = Files.createTempDirectory("msa-compensate")

        // when
        fulfillDownload(4, "event-1", ledger, FixedVideoFile(), remote, root)

        // then
        assertEquals(1, remote.removed)
        assertTrue(ledger.compensated.orEmpty().contains("원장"))
        assertEquals(null, ledger.failed)
    }

    @Test
    @DisplayName("claim이 null이면 파일을 받지 않고 수신함은 완료로 남는다")
    fun nullClaimMarksInboxDone() {
        val inbox = MarkingInbox()
        val files = CountingVideoFile()
        val handled = acceptDelivery(inbox, "media", "event-1") {
            fulfillDownload(4, "event-1", RecordingLedger(null), files, UnusedRemoteStore(), Path.of("unused"))
        }
        assertTrue(handled)
        assertEquals(0, files.saves)
        assertTrue(inbox.alreadyDone("media", "event-1"))
    }

    @Test
    @DisplayName("같은 claim이 다시 오면 파일 저장을 한 번 더 한다")
    fun resumedClaimSavesAgain() {
        val ledger = RecordingLedger(ClaimedView(4, "rjvqtbs9Q_g", "https://www.youtube.com/watch?v=rjvqtbs9Q_g"))
        val files = CountingVideoFile()
        val root = Files.createTempDirectory("msa-resume")
        fulfillDownload(4, "event-1", ledger, files, FixedRemoteStore(), root)
        fulfillDownload(4, "event-1", ledger, files, FixedRemoteStore(), root)
        assertEquals(2, files.saves)
    }
}

private class CountingVideoFile : VideoFiles {
    var saves = 0

    override fun save(video: SharedVideo, directory: Path): Path {
        saves += 1
        Files.createDirectories(directory)
        val file = directory.resolve("등대 [rjvqtbs9Q_g].mp4")
        Files.writeString(file, "video")
        return file
    }
}

private class MarkingInbox : DeliveryInbox {
    private val seen = mutableSetOf<String>()

    override fun alreadyDone(consumer: String, eventId: String): Boolean = "$consumer:$eventId" in seen

    override fun markDone(consumer: String, eventId: String) {
        seen += "$consumer:$eventId"
    }
}

private class FixedVideoFile : VideoFiles {
    override fun save(video: SharedVideo, directory: Path): Path {
        Files.createDirectories(directory)
        val file = directory.resolve("등대 [rjvqtbs9Q_g].mp4")
        Files.writeString(file, "video")
        return file
    }
}

private class FailingVideoFile : VideoFiles {
    override fun save(video: SharedVideo, directory: Path): Path =
        throw StudyException("yt-dlp를 찾지 못했습니다.", status = 502)
}

private class FixedRemoteStore : RemoteStore {
    var removed = 0

    override fun put(downloadId: Long, videoId: VideoId, file: Path): MarkSavedBody =
        MarkSavedBody(
            "youtube-download-msa",
            "${videoId.value}/$downloadId/${file.fileName}",
            file.fileName.toString(),
            5,
            "",
        )

    override fun remove(bucket: String, objectKey: String) {
        removed += 1
    }
}

private class UnusedRemoteStore : RemoteStore {
    override fun put(downloadId: Long, videoId: VideoId, file: Path): MarkSavedBody =
        error("호출되면 안 됩니다.")

    override fun remove(bucket: String, objectKey: String) {
        error("호출되면 안 됩니다.")
    }
}

private class RecordingLedger(private val claimed: ClaimedView?) : MediaLedger {
    var saved: MarkSavedBody? = null
    var failed: String? = null
    var compensated: String? = null

    override fun claim(id: Long, eventId: String): ClaimedView? = claimed?.takeIf { it.id == id }

    override fun markSaved(id: Long, body: MarkSavedBody) {
        saved = body
    }

    override fun markFailed(id: Long, message: String) {
        failed = message
    }

    override fun markCompensated(id: Long, message: String) {
        compensated = message
    }
}

private class SaveFailingLedger(private val claimed: ClaimedView?) : MediaLedger {
    var failed: String? = null
    var compensated: String? = null

    override fun claim(id: Long, eventId: String): ClaimedView? = claimed?.takeIf { it.id == id }

    override fun markSaved(id: Long, body: MarkSavedBody) {
        throw StudyException("원장 반영에 실패했습니다.", status = 502)
    }

    override fun markFailed(id: Long, message: String) {
        failed = message
    }

    override fun markCompensated(id: Long, message: String) {
        compensated = message
    }
}
