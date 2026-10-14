package com.sleekydz86.youtubedownload.video.save

import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.RemoteFile

interface DownloadClaims {
    fun claim(id: Long): ClaimedDownload?

    fun markSaved(id: Long, file: RemoteFile, title: String)

    fun markFailed(id: Long, message: String)

    fun find(id: Long): DownloadSnapshot?
}
