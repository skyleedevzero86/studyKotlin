package com.sleekydz86.youtubedownload.video.read

import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.DownloadStatus

fun interface DownloadTextSearch {
    fun searchSaved(text: String, query: PageQuery): IndexedPage
}

fun savedQuery(raw: String?): String {
    val text = raw?.trim().orEmpty()
    if (text.isEmpty()) throw DownloadException("검색어가 비어 있습니다.")
    if (text.length > 80) throw DownloadException("검색어는 80자 이하여야 합니다. 입력값: $text")
    return text
}

fun wildcardPattern(text: String): String {
    val escaped = text.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?")
    return "*$escaped*"
}

fun searchSavedDownloads(
    textSearch: DownloadTextSearch,
    links: FileLink,
    raw: String?,
    page: Int?,
    size: Int?,
): ListedPage {
    val text = savedQuery(raw)
    val query = pageQuery(page, size)
    val found = textSearch.searchSaved(text, query)
    val saved = found.copy(items = found.items.filter { it.status == DownloadStatus.SAVED })
    return listedPage(saved, links)
}
