package com.sleekydz86.youtubemsa.shared

enum class Reaction { LIKE, DISLIKE, VIEW }

fun reacted(view: DownloadView, reaction: Reaction): DownloadView {
    if (view.status != DownloadStatus.SAVED.name) {
        throw StudyException(
            "저장된 영상에만 좋아요, 싫어요, 조회를 남길 수 있습니다. 번호: ${view.id}, 상태: ${view.status}",
            status = 409,
        )
    }
    return when (reaction) {
        Reaction.LIKE -> view.copy(likeCount = view.likeCount + 1)
        Reaction.DISLIKE -> view.copy(dislikeCount = view.dislikeCount + 1)
        Reaction.VIEW -> view.copy(viewCount = view.viewCount + 1)
    }
}
