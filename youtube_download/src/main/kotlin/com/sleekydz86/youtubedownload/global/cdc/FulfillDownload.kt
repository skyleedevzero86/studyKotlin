package com.sleekydz86.youtubedownload.global.cdc

import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.global.error.shortError
import com.sleekydz86.youtubedownload.video.read.DownloadProjection
import com.sleekydz86.youtubedownload.video.save.DownloadClaims
import com.sleekydz86.youtubedownload.video.save.RemoteFileStore
import com.sleekydz86.youtubedownload.video.save.VideoFilePort
import com.sleekydz86.youtubedownload.video.titleFromFile
import org.slf4j.LoggerFactory
import java.nio.file.Path

fun fulfillDownload(
    id: Long,
    claims: DownloadClaims,
    files: VideoFilePort,
    remote: RemoteFileStore,
    lock: DownloadLock,
    workRoot: Path,
) {
    val log = LoggerFactory.getLogger("fulfillDownload")
    lock.hold(id) {
        val claimed = claims.claim(id) ?: return@hold
        val directory = workRoot.resolve(id.toString())
        try {
            log.info("영상 파일 저장 시작 id={} videoId={}", id, claimed.video.id.value)
            val saved = files.save(claimed.video, directory)
            val stored = remote.put(id, claimed.video.id, saved.file)
            claims.markSaved(id, stored, titleFromFile(saved.file.fileName.toString(), claimed.video.id))
            log.info("영상 파일 저장 완료 id={} objectKey={}", id, stored.objectKey)
        } catch (ex: DownloadException) {
            claims.markFailed(id, shortError(ex.message ?: "영상 저장에 실패했습니다."))
            log.warn("영상 파일 저장 실패 id={}", id, ex)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}

fun projectDownload(id: Long, claims: DownloadClaims, projection: DownloadProjection) {
    val snapshot = claims.find(id) ?: return
    projection.upsert(snapshot)
}
