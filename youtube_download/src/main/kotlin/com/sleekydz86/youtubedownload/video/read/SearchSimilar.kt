package com.sleekydz86.youtubedownload.video.read

import com.sleekydz86.youtubedownload.global.embedding.CosineSimilarity
import com.sleekydz86.youtubedownload.global.embedding.HashingEmbedder
import com.sleekydz86.youtubedownload.global.embedding.SearchLimits
import com.sleekydz86.youtubedownload.global.embedding.VectorCodec
import com.sleekydz86.youtubedownload.global.embedding.downloadText
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus

data class VectorHit(val id: Long, val esScore: Double)

fun interface DownloadVectorSearch {
    fun nearest(vector: FloatArray, k: Int): List<VectorHit>
}

data class SimilarQuery(val query: String = "", val k: Int? = null)

data class SimilarDownload(
    val item: ListedDownload,
    val esScore: Double,
    val cosine: Double,
)

data class SimilarPage(
    val dims: Int,
    val similarity: String,
    val k: Int,
    val items: List<SimilarDownload>,
)

fun searchSimilar(
    embedder: HashingEmbedder,
    nearest: DownloadVectorSearch,
    find: (Long) -> DownloadSnapshot?,
    links: FileLink,
    query: SimilarQuery,
    dims: Int,
    similarity: String,
): SimilarPage {
    val limit = SearchLimits.normalizeK(query.k)
    val vector = embedder.embed(query.query.trim())
    val items = nearest.nearest(vector, limit).mapNotNull { hit ->
        similarItem(hit, vector, find, links, embedder)
    }
    return SimilarPage(dims, similarity, limit, items)
}

fun storedVector(snapshot: DownloadSnapshot, embedder: HashingEmbedder): FloatArray {
    if (snapshot.embeddingJson.isNotBlank()) return VectorCodec.decode(snapshot.embeddingJson)
    return embedder.embed(downloadText(snapshot.videoId.value, snapshot.title, snapshot.fileName, snapshot.errorMessage))
}

internal fun similarItem(
    hit: VectorHit,
    query: FloatArray,
    find: (Long) -> DownloadSnapshot?,
    links: FileLink,
    embedder: HashingEmbedder,
): SimilarDownload? {
    val snapshot = find(hit.id) ?: return null
    if (snapshot.status != DownloadStatus.SAVED) return null
    return SimilarDownload(
        item = listedFromSnapshot(snapshot, links),
        esScore = hit.esScore,
        cosine = CosineSimilarity.score(query, storedVector(snapshot, embedder)),
    )
}
