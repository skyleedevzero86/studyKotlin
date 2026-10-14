package com.sleekydz86.youtubedownload.video.read

import com.sleekydz86.youtubedownload.global.embedding.HashingEmbedder
import com.sleekydz86.youtubedownload.global.embedding.SearchLimits
import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import org.springframework.http.HttpStatus

const val recommendLimit = 5

fun interface SavedCatalog {
    fun saved(): List<DownloadSnapshot>
}

fun recommendationAnchor(saved: List<DownloadSnapshot>, anchorId: Long?): DownloadSnapshot? {
    val ready = saved.filter { it.status == DownloadStatus.SAVED }
    if (anchorId == null) {
        return ready.maxWithOrNull(compareBy<DownloadSnapshot> { it.likeCount }.thenBy { it.viewCount }.thenBy { it.updatedAt })
    }
    return ready.find { it.id == anchorId }
        ?: throw DownloadException("저장된 영상을 찾지 못했습니다. 번호: $anchorId", status = HttpStatus.NOT_FOUND)
}

fun recommendDownloads(
    anchorId: Long?,
    catalog: SavedCatalog,
    nearest: DownloadVectorSearch,
    find: (Long) -> DownloadSnapshot?,
    links: FileLink,
    embedder: HashingEmbedder,
    dims: Int,
    similarity: String,
): SimilarPage {
    val anchor = recommendationAnchor(catalog.saved(), anchorId)
        ?: return SimilarPage(dims, similarity, recommendLimit, emptyList())
    val vector = storedVector(anchor, embedder)
    val items = nearest.nearest(vector, SearchLimits.normalizeK(20))
        .mapNotNull { hit -> similarItem(hit, vector, find, links, embedder) }
        .filter { it.item.id != anchor.id }
        .sortedByDescending { it.cosine }
        .take(recommendLimit)
    return SimilarPage(dims, similarity, recommendLimit, items)
}
