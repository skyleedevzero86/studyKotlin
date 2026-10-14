package com.sleekydz86.youtubedownload.video.save

import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.RemoteFile
import com.sleekydz86.youtubedownload.video.SharedVideo
import com.sleekydz86.youtubedownload.video.VideoId
import java.nio.file.Path

data class ClaimedDownload(val id: Long, val video: SharedVideo)

interface DownloadClaims {
    fun claim(id: Long): ClaimedDownload?

    fun markSaved(id: Long, file: RemoteFile, title: String)

    fun markFailed(id: Long, message: String)

    fun find(id: Long): DownloadSnapshot?
}

fun interface RemoteFileStore {
    fun put(downloadId: Long, videoId: VideoId, file: Path): RemoteFile
}
