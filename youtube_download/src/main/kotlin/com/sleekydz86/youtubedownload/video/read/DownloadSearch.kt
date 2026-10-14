package com.sleekydz86.youtubedownload.video.read

fun interface DownloadSearch {
    fun search(query: PageQuery): IndexedPage
}
