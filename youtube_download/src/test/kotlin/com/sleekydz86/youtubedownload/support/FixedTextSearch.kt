package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.read.DownloadTextSearch
import com.sleekydz86.youtubedownload.video.read.IndexedDownload
import com.sleekydz86.youtubedownload.video.read.IndexedPage
import com.sleekydz86.youtubedownload.video.read.PageQuery

class FixedTextSearch(private val rows: List<IndexedDownload>) : DownloadTextSearch {
    val texts = mutableListOf<String>()

    override fun searchSaved(text: String, query: PageQuery): IndexedPage {
        texts += text
        val matched = rows.filter { row ->
            row.status == DownloadStatus.SAVED && (row.title?.contains(text) == true || row.videoId.contains(text))
        }
        return IndexedPage(query.page, query.size, matched.size.toLong(), matched)
    }
}
