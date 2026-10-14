package com.sleekydz86.youtubedownload.unit.embedding

import com.sleekydz86.youtubedownload.global.elasticsearch.DownloadDocument
import com.sleekydz86.youtubedownload.global.elasticsearch.indexedDownload
import com.sleekydz86.youtubedownload.global.embedding.ClusterVersionGate
import com.sleekydz86.youtubedownload.global.embedding.DenseVectorMapping
import com.sleekydz86.youtubedownload.global.embedding.HashingEmbedder
import com.sleekydz86.youtubedownload.global.embedding.IndexOptionRules
import com.sleekydz86.youtubedownload.global.embedding.SearchLimits
import com.sleekydz86.youtubedownload.global.embedding.VectorCodec
import com.sleekydz86.youtubedownload.global.embedding.downloadText
import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.support.MemoryLinks
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.DownloadStatus
import com.sleekydz86.youtubedownload.video.VideoId
import com.sleekydz86.youtubedownload.video.read.DownloadVectorSearch
import com.sleekydz86.youtubedownload.video.read.SimilarQuery
import com.sleekydz86.youtubedownload.video.read.VectorHit
import com.sleekydz86.youtubedownload.video.read.searchSimilar
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VectorFeaturesTest {
    @Test
    fun versionGateAcceptsNineFiveAndRejectsOlder() {
        ClusterVersionGate.assertLaterVectorStack("9.5.5")
        val eight = assertFailsWith<DownloadException> { ClusterVersionGate.assertLaterVectorStack("8.15.3") }
        val nineFour = assertFailsWith<DownloadException> { ClusterVersionGate.assertLaterVectorStack("9.4.2") }
        assertTrue(eight.message!!.contains("8.15.3"))
        assertTrue(nineFour.message!!.contains("bbq_disk"))
    }

    @Test
    fun mappingUsesVectorDbDocumentAndBbqHnsw() {
        val json = DenseVectorMapping.createIndexJson(64, "cosine", "bbq_hnsw", 16, 100)
        assertTrue(json.contains("vectordb_document"))
        assertTrue(json.contains("bbq_hnsw"))
        assertTrue(json.contains("\"m\": 16"))
        assertTrue(json.contains("\"ef_construction\": 100"))
    }

    @Test
    fun diskMappingOmitsHnswParameters() {
        val json = DenseVectorMapping.createIndexJson(64, "cosine", "bbq_disk", 16, 100)
        assertTrue(json.contains("bbq_disk"))
        assertTrue(!json.contains("ef_construction"))
    }

    @Test
    fun embedderIsDeterministicAndDownloadTextKeepsVideoId() {
        val embedder = HashingEmbedder(64)
        val text = downloadText("rjvqtbs9Q_g", "  ", null)
        assertEquals("rjvqtbs9Q_g", text)
        assertTrue(embedder.embed(text).contentEquals(embedder.embed(text)))
        assertFailsWith<DownloadException> { IndexOptionRules.validate(64, "ivf", "cosine", 16, 100) }
    }

    @Test
    fun searchLimitsClampCandidates() {
        assertEquals(5, SearchLimits.normalizeK(null))
        assertEquals(100, SearchLimits.numCandidates(5, 100))
        assertEquals(20, SearchLimits.numCandidates(20, 8))
        assertFailsWith<DownloadException> { SearchLimits.normalizeK(21) }
    }

    @Test
    fun similarSearchScoresFromStoredEmbedding() {
        val embedder = HashingEmbedder(64)
        val vector = embedder.embed("라이브 rjvqtbs9Q_g")
        val snapshot = savedSnapshot(VectorCodec.encode(vector))
        val page = searchSimilar(
            embedder,
            FixedHits(listOf(VectorHit(4, 0.91))),
            SnapshotLookup(snapshot)::find,
            MemoryLinks(),
            SimilarQuery("라이브 rjvqtbs9Q_g", 5),
            64,
            "cosine",
        )
        assertEquals(64, page.dims)
        assertEquals(0.91, page.items.single().esScore)
        assertTrue(page.items.single().cosine > 0.99)
        assertEquals("http://127.0.0.1:9000/youtube-download/a.mp4", page.items.single().item.fileUrl)
    }

    @Test
    fun similarSearchSkipsMissingRows() {
        val page = searchSimilar(
            HashingEmbedder(64),
            FixedHits(listOf(VectorHit(9, 0.2))),
            MissingLookup()::find,
            MemoryLinks(),
            SimilarQuery("rjvqtbs9Q_g"),
            64,
            "cosine",
        )
        assertTrue(page.items.isEmpty())
    }

    @Test
    fun documentMappingDropsInvalidSource() {
        val source = DownloadDocument()
        source.id = 4
        source.videoId = "rjvqtbs9Q_g"
        source.status = "SAVED"
        source.title = "라이브"
        source.updatedAt = "2026-10-14T08:00:00Z"
        assertEquals("라이브", indexedDownload(source)?.title)
        source.status = "NOPE"
        assertNull(indexedDownload(source))
    }

    private fun savedSnapshot(embedding: String) = DownloadSnapshot(
        id = 4,
        videoId = VideoId("rjvqtbs9Q_g"),
        title = "라이브",
        status = DownloadStatus.SAVED,
        bucket = "youtube-download",
        objectKey = "a.mp4",
        fileName = "a.mp4",
        sizeBytes = 12,
        errorMessage = null,
        updatedAt = Instant.parse("2026-10-14T08:00:00Z"),
        embeddingJson = embedding,
    )
}

private class FixedHits(private val hits: List<VectorHit>) : DownloadVectorSearch {
    override fun nearest(vector: FloatArray, k: Int): List<VectorHit> = hits.take(k)
}

private class SnapshotLookup(private val snapshot: DownloadSnapshot) {
    fun find(id: Long): DownloadSnapshot? = snapshot.takeIf { it.id == id }
}

private class MissingLookup {
    fun find(id: Long): DownloadSnapshot? = null
}
