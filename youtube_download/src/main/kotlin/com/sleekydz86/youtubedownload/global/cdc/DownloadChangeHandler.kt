package com.sleekydz86.youtubedownload.global.cdc

import com.sleekydz86.youtubedownload.global.env.StudyProperties
import com.sleekydz86.youtubedownload.video.read.DownloadProjection
import com.sleekydz86.youtubedownload.video.save.DownloadClaims
import com.sleekydz86.youtubedownload.video.save.RemoteFileStore
import com.sleekydz86.youtubedownload.video.save.VideoFilePort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class DownloadChangeHandler(
    private val claims: DownloadClaims,
    private val files: VideoFilePort,
    private val remote: RemoteFileStore,
    private val projection: DownloadProjection,
    private val lock: DownloadLock,
    properties: StudyProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val workRoot = Path.of(properties.workDirectory)

    fun handle(change: DownloadChange) {
        log.info("CDC 저장 반영 id={} status={}", change.id, change.status)
        projectDownload(change.id, claims, projection)
        if (changeStep(change.status) == ChangeStep.FULFILL) {
            fulfillDownload(change.id, claims, files, remote, lock, workRoot)
        }
    }
}
