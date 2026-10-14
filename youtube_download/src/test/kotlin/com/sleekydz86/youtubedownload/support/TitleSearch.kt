package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.read.DownloadTextSearch
import com.sleekydz86.youtubedownload.video.read.IndexedDownload
import com.sleekydz86.youtubedownload.video.read.IndexedPage
import com.sleekydz86.youtubedownload.video.read.PageQuery

class TitleSearch(private val rows: List<DownloadSnapshot>) : DownloadTextSearch {
    override fun searchSaved(text: String, query: PageQuery): IndexedPage {
        val matched = rows.filter { it.status == DownloadStatus.SAVED && it.title?.contains(text) == true }
        val items = matched.map { row ->
            IndexedDownload(
                id = row.id,
                videoId = row.videoId.value,
                title = row.title,
                status = row.status,
                bucket = row.bucket,
                objectKey = row.objectKey,
                fileName = row.fileName,
                sizeBytes = row.sizeBytes,
                errorMessage = row.errorMessage,
                updatedAt = row.updatedAt.toString(),
                likeCount = row.likeCount,
            )
        }
        return IndexedPage(query.page, query.size, items.size.toLong(), items)
    }
}
