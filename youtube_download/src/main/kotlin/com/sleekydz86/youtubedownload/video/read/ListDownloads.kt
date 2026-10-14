package com.sleekydz86.youtubedownload.video.read

import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus

data class PageQuery(val page: Int, val size: Int)

data class IndexedDownload(
    val id: Long,
    val videoId: String,
    val title: String?,
    val status: DownloadStatus,
    val bucket: String?,
    val objectKey: String?,
    val fileName: String?,
    val sizeBytes: Long?,
    val errorMessage: String?,
    val updatedAt: String,
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val viewCount: Int = 0,
)

data class IndexedPage(
    val page: Int,
    val size: Int,
    val total: Long,
    val items: List<IndexedDownload>,
)

data class ListedDownload(
    val id: Long,
    val videoId: String,
    val title: String?,
    val status: String,
    val fileName: String?,
    val sizeBytes: Long?,
    val errorMessage: String?,
    val updatedAt: String,
    val fileUrl: String?,
    val likeCount: Int = 0,
    val dislikeCount: Int = 0,
    val viewCount: Int = 0,
)

data class ListedPage(
    val page: Int,
    val size: Int,
    val total: Long,
    val items: List<ListedDownload>,
)

fun interface DownloadSearch {
    fun search(query: PageQuery): IndexedPage
}

fun interface DownloadProjection {
    fun upsert(snapshot: DownloadSnapshot)
}

fun interface FileLink {
    fun link(bucket: String, objectKey: String): String
}

fun pageQuery(page: Int?, size: Int?): PageQuery {
    val resolvedPage = page ?: 0
    val resolvedSize = size ?: 10
    if (resolvedPage !in 0..200) {
        throw DownloadException("page는 0 이상 200 이하여야 합니다. 입력값: $resolvedPage")
    }
    if (resolvedSize !in 1..50) {
        throw DownloadException("size는 1 이상 50 이하여야 합니다. 입력값: $resolvedSize")
    }
    return PageQuery(resolvedPage, resolvedSize)
}

fun listDownloads(search: DownloadSearch, links: FileLink, query: PageQuery): ListedPage =
    listedPage(search.search(query), links)

fun listedPage(found: IndexedPage, links: FileLink): ListedPage =
    ListedPage(
        page = found.page,
        size = found.size,
        total = found.total,
        items = found.items.map { item -> listedDownload(item, links) },
    )

fun listedFromSnapshot(snapshot: DownloadSnapshot, links: FileLink): ListedDownload =
    listedDownload(
        IndexedDownload(
            id = snapshot.id,
            videoId = snapshot.videoId.value,
            title = snapshot.title,
            status = snapshot.status,
            bucket = snapshot.bucket,
            objectKey = snapshot.objectKey,
            fileName = snapshot.fileName,
            sizeBytes = snapshot.sizeBytes,
            errorMessage = snapshot.errorMessage,
            updatedAt = snapshot.updatedAt.toString(),
            likeCount = snapshot.likeCount,
            dislikeCount = snapshot.dislikeCount,
            viewCount = snapshot.viewCount,
        ),
        links,
    )

private fun listedDownload(item: IndexedDownload, links: FileLink): ListedDownload =
    ListedDownload(
        id = item.id,
        videoId = item.videoId,
        title = item.title,
        status = item.status.name,
        fileName = item.fileName,
        sizeBytes = item.sizeBytes,
        errorMessage = item.errorMessage,
        updatedAt = item.updatedAt,
        fileUrl = fileUrl(item, links),
        likeCount = item.likeCount,
        dislikeCount = item.dislikeCount,
        viewCount = item.viewCount,
    )

private fun fileUrl(item: IndexedDownload, links: FileLink): String? {
    val bucket = item.bucket
    val objectKey = item.objectKey
    if (item.status != DownloadStatus.SAVED || bucket == null || objectKey == null) return null
    return links.link(bucket, objectKey)
}
