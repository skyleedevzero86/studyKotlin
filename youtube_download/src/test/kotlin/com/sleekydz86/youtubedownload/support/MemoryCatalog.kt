package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.read.SavedCatalog
import com.sleekydz86.youtubedownload.video.react.DownloadReactions
import com.sleekydz86.youtubedownload.video.react.Reaction
import com.sleekydz86.youtubedownload.video.react.reacted

class MemoryCatalog(rows: List<DownloadSnapshot>) : SavedCatalog, DownloadReactions {
    var rows: List<DownloadSnapshot> = rows

    override fun saved(): List<DownloadSnapshot> = rows.filter { it.status == DownloadStatus.SAVED }

    fun find(id: Long): DownloadSnapshot? = rows.find { it.id == id }

    override fun react(id: Long, reaction: Reaction): DownloadSnapshot {
        val current = find(id) ?: error("없음")
        val next = reacted(current, reaction)
        rows = rows.map { if (it.id == id) next else it }
        return next
    }
}
