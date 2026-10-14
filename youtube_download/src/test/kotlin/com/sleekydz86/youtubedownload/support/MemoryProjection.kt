package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.read.DownloadProjection

class MemoryProjection : DownloadProjection {
    val saved = mutableListOf<Long>()

    override fun upsert(snapshot: DownloadSnapshot) {
        saved += snapshot.id
    }
}
