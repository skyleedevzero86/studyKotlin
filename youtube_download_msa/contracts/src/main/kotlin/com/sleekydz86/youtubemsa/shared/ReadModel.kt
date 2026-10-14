package com.sleekydz86.youtubemsa.shared

const val recommendLimit = 5

fun savedQuery(raw: String?): String {
    val text = raw?.trim().orEmpty()
    if (text.isEmpty()) throw StudyException("검색어가 비어 있습니다.")
    if (text.length > 80) throw StudyException("검색어는 80자 이하여야 합니다. 입력값: $text")
    return text
}

fun wildcardPattern(text: String): String {
    val escaped = text.replace("\\", "\\\\").replace("*", "\\*").replace("?", "\\?")
    return "*$escaped*"
}

fun listedFrom(view: DownloadView, fileUrl: String?): ListedDownload =
    ListedDownload(
        id = view.id,
        videoId = view.videoId,
        title = view.title,
        status = view.status,
        fileName = view.fileName,
        sizeBytes = view.sizeBytes,
        errorMessage = view.errorMessage,
        updatedAt = view.updatedAt,
        fileUrl = if (view.status == DownloadStatus.SAVED.name) fileUrl else null,
        likeCount = view.likeCount,
        dislikeCount = view.dislikeCount,
        viewCount = view.viewCount,
    )

fun storedVector(view: DownloadView, embedder: HashingEmbedder): FloatArray {
    if (view.embeddingJson.isNotBlank()) return VectorCodec.decode(view.embeddingJson)
    return embedder.embed(downloadText(view.videoId, view.title, view.fileName, view.errorMessage))
}

fun recommendationAnchor(saved: List<DownloadView>, anchorId: Long?): DownloadView? {
    val ready = saved.filter { it.status == DownloadStatus.SAVED.name }
    if (anchorId == null) {
        return ready.maxWithOrNull(compareBy<DownloadView> { it.likeCount }.thenBy { it.viewCount }.thenBy { it.updatedAt })
    }
    return ready.find { it.id == anchorId }
        ?: throw StudyException("저장된 영상을 찾지 못했습니다. 번호: $anchorId", status = 404)
}

fun recommendDownloads(
    anchorId: Long?,
    saved: List<DownloadView>,
    nearest: (FloatArray, Int) -> List<VectorHit>,
    find: (Long) -> DownloadView?,
    fileUrl: (DownloadView) -> String?,
    embedder: HashingEmbedder,
    dims: Int,
    similarity: String,
): SimilarPage {
    val anchor = recommendationAnchor(saved, anchorId)
        ?: return SimilarPage(dims, similarity, recommendLimit, emptyList())
    val vector = storedVector(anchor, embedder)
    val items = nearest(vector, SearchLimits.normalizeK(20))
        .mapNotNull { hit -> scored(hit, vector, find, fileUrl, embedder) }
        .filter { it.item.id != anchor.id }
        .sortedByDescending { it.cosine }
        .take(recommendLimit)
    return SimilarPage(dims, similarity, recommendLimit, items)
}

fun searchSimilar(
    embedder: HashingEmbedder,
    nearest: (FloatArray, Int) -> List<VectorHit>,
    find: (Long) -> DownloadView?,
    fileUrl: (DownloadView) -> String?,
    query: SimilarQuery,
    dims: Int,
    similarity: String,
): SimilarPage {
    val limit = SearchLimits.normalizeK(query.k)
    val vector = embedder.embed(query.query.trim())
    val items = nearest(vector, limit).mapNotNull { hit -> scored(hit, vector, find, fileUrl, embedder) }
    return SimilarPage(dims, similarity, limit, items)
}

private fun scored(
    hit: VectorHit,
    query: FloatArray,
    find: (Long) -> DownloadView?,
    fileUrl: (DownloadView) -> String?,
    embedder: HashingEmbedder,
): SimilarDownload? {
    val view = find(hit.id) ?: return null
    if (view.status != DownloadStatus.SAVED.name) return null
    return SimilarDownload(
        item = listedFrom(view, fileUrl(view)),
        esScore = hit.esScore,
        cosine = CosineSimilarity.score(query, storedVector(view, embedder)),
    )
}
