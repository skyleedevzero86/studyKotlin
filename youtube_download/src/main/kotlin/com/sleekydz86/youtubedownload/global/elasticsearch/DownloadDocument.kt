package com.sleekydz86.youtubedownload.global.elasticsearch

import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.downloadStatus
import com.sleekydz86.youtubedownload.video.read.IndexedDownload

class DownloadDocument {
    var id: Long = 0
    var videoId: String = ""
    var title: String? = null
    var status: String = ""
    var bucket: String? = null
    var objectKey: String? = null
    var fileName: String? = null
    var sizeBytes: Long? = null
    var errorMessage: String? = null
    var updatedAt: String = ""
    var likeCount: Int = 0
    var dislikeCount: Int = 0
    var viewCount: Int = 0
    var embedding: List<Float> = emptyList()
}

fun downloadDocument(snapshot: DownloadSnapshot, embedding: FloatArray): DownloadDocument {
    val document = DownloadDocument()
    document.id = snapshot.id
    document.videoId = snapshot.videoId.value
    document.title = snapshot.title
    document.status = snapshot.status.name
    document.bucket = snapshot.bucket
    document.objectKey = snapshot.objectKey
    document.fileName = snapshot.fileName
    document.sizeBytes = snapshot.sizeBytes
    document.errorMessage = snapshot.errorMessage
    document.updatedAt = snapshot.updatedAt.toString()
    document.likeCount = snapshot.likeCount
    document.dislikeCount = snapshot.dislikeCount
    document.viewCount = snapshot.viewCount
    document.embedding = embedding.toList()
    return document
}

fun indexedDownload(source: DownloadDocument?): IndexedDownload? {
    if (source == null) return null
    val status = downloadStatus(source.status) ?: return null
    if (source.id <= 0 || !VideoId.isValid(source.videoId)) return null
    return IndexedDownload(
        id = source.id,
        videoId = source.videoId,
        title = source.title?.takeIf { it.isNotEmpty() },
        status = status,
        bucket = source.bucket?.takeIf { it.isNotEmpty() },
        objectKey = source.objectKey?.takeIf { it.isNotEmpty() },
        fileName = source.fileName?.takeIf { it.isNotEmpty() },
        sizeBytes = source.sizeBytes,
        errorMessage = source.errorMessage?.takeIf { it.isNotEmpty() },
        updatedAt = source.updatedAt,
        likeCount = source.likeCount,
        dislikeCount = source.dislikeCount,
        viewCount = source.viewCount,
    )
}
