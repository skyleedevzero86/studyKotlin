package com.sleekydz86.youtubedownload.video.react

import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import org.springframework.http.HttpStatus

enum class Reaction { LIKE, DISLIKE, VIEW }

fun reacted(snapshot: DownloadSnapshot, reaction: Reaction): DownloadSnapshot {
    if (snapshot.status != DownloadStatus.SAVED) {
        throw DownloadException(
            "저장된 영상에만 좋아요, 싫어요, 조회를 남길 수 있습니다. 번호: ${snapshot.id}, 상태: ${snapshot.status}",
            status = HttpStatus.CONFLICT,
        )
    }
    return when (reaction) {
        Reaction.LIKE -> snapshot.copy(likeCount = snapshot.likeCount + 1)
        Reaction.DISLIKE -> snapshot.copy(dislikeCount = snapshot.dislikeCount + 1)
        Reaction.VIEW -> snapshot.copy(viewCount = snapshot.viewCount + 1)
    }
}
