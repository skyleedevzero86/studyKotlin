package com.sleekydz86.youtubemsa.media

import com.sleekydz86.youtubemsa.shared.MarkSavedBody
import com.sleekydz86.youtubemsa.shared.SharedVideo
import com.sleekydz86.youtubemsa.shared.VideoId
import com.sleekydz86.youtubemsa.shared.shortError
import com.sleekydz86.youtubemsa.shared.shouldCompensate
import com.sleekydz86.youtubemsa.shared.titleFromFile
import org.slf4j.LoggerFactory
import java.nio.file.Path

fun fulfillDownload(
    id: Long,
    eventId: String,
    ledger: MediaLedger,
    files: VideoFiles,
    remote: RemoteStore,
    workRoot: Path,
) {
    val log = LoggerFactory.getLogger("fulfillDownload")
    val claimed = ledger.claim(id, eventId) ?: return
    val directory = workRoot.resolve(id.toString())
    val video = SharedVideo(VideoId(claimed.videoId))
    var stored: MarkSavedBody? = null
    try {
        log.info("영상 파일 저장 시작 id={} videoId={} eventId={}", id, claimed.videoId, eventId)
        val file = files.save(video, directory)
        stored = remote.put(id, video.id, file).copy(title = titleFromFile(file.fileName.toString(), video.id))
        ledger.markSaved(id, stored)
        log.info("영상 파일 저장 완료 id={} objectKey={}", id, stored.objectKey)
    } catch (ex: Exception) {
        compensate(id, stored, ledger, remote, ex)
    } finally {
        directory.toFile().deleteRecursively()
    }
}

private fun compensate(
    id: Long,
    stored: MarkSavedBody?,
    ledger: MediaLedger,
    remote: RemoteStore,
    cause: Exception,
) {
    val log = LoggerFactory.getLogger("fulfillDownload")
    val message = shortError(cause.message ?: "영상 저장에 실패했습니다.")
    try {
        if (stored != null && shouldCompensate(uploaded = true, saveFailed = true)) {
            remote.remove(stored.bucket, stored.objectKey)
            ledger.markCompensated(id, message)
        } else {
            ledger.markFailed(id, message)
        }
        log.warn("영상 파일 저장 실패 id={}", id, cause)
    } catch (ex: Exception) {
        log.error("사가 보상 처리 실패 id={}", id, ex)
        throw ex
    }
}
