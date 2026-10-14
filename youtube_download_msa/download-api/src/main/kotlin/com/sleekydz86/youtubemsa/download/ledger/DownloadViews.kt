package com.sleekydz86.youtubemsa.download.ledger

import com.sleekydz86.youtubemsa.download.persistence.DownloadEntity
import com.sleekydz86.youtubemsa.shared.DownloadView

fun viewOf(entity: DownloadEntity): DownloadView =
    DownloadView(
        id = entity.id,
        videoId = entity.videoId,
        watchUrl = entity.watchUrl,
        title = entity.title,
        status = entity.status,
        bucket = entity.bucket,
        objectKey = entity.objectKey,
        fileName = entity.fileName,
        sizeBytes = entity.sizeBytes,
        errorMessage = entity.errorMessage,
        updatedAt = entity.updatedAt.toString(),
        embeddingJson = entity.embeddingJson,
        likeCount = entity.likeCount,
        dislikeCount = entity.dislikeCount,
        viewCount = entity.viewCount,
    )
