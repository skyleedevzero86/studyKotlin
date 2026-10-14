package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.video.read.DownloadSearch
import com.sleekydz86.youtubedownload.video.read.IndexedPage
import com.sleekydz86.youtubedownload.video.read.PageQuery

class MemorySearch(private val page: IndexedPage) : DownloadSearch {
    override fun search(query: PageQuery): IndexedPage = page
}
