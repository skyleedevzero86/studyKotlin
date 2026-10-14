package com.sleekydz86.youtubedownload.video.read

import com.sleekydz86.youtubedownload.video.DownloadSnapshot

fun interface DownloadProjection {
    fun upsert(snapshot: DownloadSnapshot)
}
