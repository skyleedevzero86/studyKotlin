package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.DownloadSnapshot

class SnapshotLookup(private val snapshot: DownloadSnapshot) {
    fun find(id: Long): DownloadSnapshot? = snapshot.takeIf { it.id == id }
}
