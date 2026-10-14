package com.sleekydz86.youtubedownload.video.read

import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus

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
