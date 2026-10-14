package com.sleekydz86.youtubedownload.support

import com.sleekydz86.youtubedownload.global.embedding.CosineSimilarity
import com.sleekydz86.youtubedownload.global.embedding.HashingEmbedder
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.read.DownloadVectorSearch
import com.sleekydz86.youtubedownload.video.read.VectorHit
import com.sleekydz86.youtubedownload.video.read.storedVector

class CosineNearest(
    private val rows: List<DownloadSnapshot>,
    private val embedder: HashingEmbedder,
) : DownloadVectorSearch {
    override fun nearest(vector: FloatArray, k: Int): List<VectorHit> =
        rows.map { row -> VectorHit(row.id, CosineSimilarity.score(vector, storedVector(row, embedder))) }
            .sortedByDescending { it.esScore }
            .take(k)
}
