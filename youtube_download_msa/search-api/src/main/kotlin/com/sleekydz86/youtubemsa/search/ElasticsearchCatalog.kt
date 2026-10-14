package com.sleekydz86.youtubemsa.search

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch.core.search.Hit
import com.sleekydz86.youtubemsa.shared.DownloadStatus
import com.sleekydz86.youtubemsa.shared.DownloadView
import com.sleekydz86.youtubemsa.shared.HashingEmbedder
import com.sleekydz86.youtubemsa.shared.ListedDownload
import com.sleekydz86.youtubemsa.shared.ListedPage
import com.sleekydz86.youtubemsa.shared.PageQuery
import com.sleekydz86.youtubemsa.shared.SearchLimits
import com.sleekydz86.youtubemsa.shared.StudyException
import com.sleekydz86.youtubemsa.shared.VectorCodec
import com.sleekydz86.youtubemsa.shared.VectorHit
import com.sleekydz86.youtubemsa.shared.downloadText
import com.sleekydz86.youtubemsa.shared.wildcardPattern
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ElasticsearchCatalog(
    private val client: ElasticsearchClient,
    private val embedder: HashingEmbedder,
    private val links: FileLinks,
    properties: SearchProperties,
) : DownloadCatalog {
    private val elasticsearch = properties.elasticsearch

    override fun list(query: PageQuery): ListedPage =
        try {
            val response = client.search({ request ->
                request.index(elasticsearch.index)
                    .from(query.page * query.size)
                    .size(query.size)
                    .sort { sort -> sort.field { field -> field.field("updatedAt").order(SortOrder.Desc) } }
                    .query { searchQuery -> searchQuery.matchAll { match -> match } }
            }, DownloadDocument::class.java)
            pageOf(query, response.hits().total()?.value() ?: 0, response.hits().hits().mapNotNull { listed(it.source()) })
        } catch (ex: StudyException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "목록 조회", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "목록 조회", ex)
        }

    override fun searchSaved(text: String, query: PageQuery): ListedPage {
        val pattern = wildcardPattern(text)
        return try {
            val response = client.search({ request ->
                request.index(elasticsearch.index)
                    .from(query.page * query.size)
                    .size(query.size)
                    .sort { sort -> sort.field { field -> field.field("updatedAt").order(SortOrder.Desc) } }
                    .query { searchQuery ->
                        searchQuery.bool { bool ->
                            bool.filter { filter -> filter.term { term -> term.field("status").value("SAVED") } }
                            bool.must { must ->
                                must.bool { inner ->
                                    inner.should { should -> should.wildcard { it.field("title").value(pattern).caseInsensitive(true) } }
                                    inner.should { should -> should.wildcard { it.field("fileName").value(pattern).caseInsensitive(true) } }
                                    inner.should { should -> should.wildcard { it.field("videoId").value(pattern).caseInsensitive(true) } }
                                    inner.minimumShouldMatch("1")
                                }
                            }
                        }
                    }
            }, DownloadDocument::class.java)
            pageOf(query, response.hits().total()?.value() ?: 0, response.hits().hits().mapNotNull { listed(it.source()) })
        } catch (ex: StudyException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "저장 검색", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "저장 검색", ex)
        }
    }

    override fun nearest(vector: FloatArray, k: Int): List<VectorHit> {
        val candidates = SearchLimits.numCandidates(k, elasticsearch.numCandidates)
        return try {
            val response = client.search({ request ->
                request.index(elasticsearch.index)
                    .knn { knn ->
                        knn.field("embedding")
                            .queryVector(vector.toList())
                            .k(k)
                            .numCandidates(candidates)
                            .filter { filter -> filter.term { term -> term.field("status").value("SAVED") } }
                    }
                    .source { source -> source.fetch(false) }
            }, Void::class.java)
            response.hits().hits().mapNotNull(::vectorHit)
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "kNN 검색", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "kNN 검색", ex)
        }
    }

    override fun upsert(view: DownloadView) {
        try {
            client.index { request ->
                request.index(elasticsearch.index)
                    .id(view.id.toString())
                    .document(documentOf(view, vectorOf(view)))
                    .refresh(Refresh.True)
            }
        } catch (ex: StudyException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "색인 id=${view.id}", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "색인 id=${view.id}", ex)
        }
    }

    private fun vectorOf(view: DownloadView): FloatArray {
        if (view.embeddingJson.isNotBlank()) return VectorCodec.decode(view.embeddingJson)
        return embedder.embed(downloadText(view.videoId, view.title, view.fileName, view.errorMessage))
    }

    private fun vectorHit(hit: Hit<Void>): VectorHit? {
        val id = hit.id()?.toLongOrNull() ?: return null
        return VectorHit(id, hit.score() ?: 0.0)
    }

    private fun pageOf(query: PageQuery, total: Long, items: List<ListedDownload>): ListedPage =
        ListedPage(query.page, query.size, total, items)

    private fun listed(source: DownloadDocument?): ListedDownload? {
        if (source == null || source.id <= 0 || source.videoId.length != 11) return null
        val fileUrl = if (source.status == DownloadStatus.SAVED.name) linkOf(source) else null
        return ListedDownload(
            id = source.id,
            videoId = source.videoId,
            title = source.title?.takeIf { it.isNotEmpty() },
            status = source.status,
            fileName = source.fileName?.takeIf { it.isNotEmpty() },
            sizeBytes = source.sizeBytes,
            errorMessage = source.errorMessage?.takeIf { it.isNotEmpty() },
            updatedAt = source.updatedAt,
            fileUrl = fileUrl,
            likeCount = source.likeCount,
            dislikeCount = source.dislikeCount,
            viewCount = source.viewCount,
        )
    }

    private fun linkOf(source: DownloadDocument): String? {
        val bucket = source.bucket?.takeIf { it.isNotBlank() } ?: return null
        val objectKey = source.objectKey?.takeIf { it.isNotBlank() } ?: return null
        return links.link(bucket, objectKey)
    }
}

private fun documentOf(view: DownloadView, embedding: FloatArray): DownloadDocument {
    val document = DownloadDocument()
    document.id = view.id
    document.videoId = view.videoId
    document.title = view.title
    document.status = view.status
    document.bucket = view.bucket
    document.objectKey = view.objectKey
    document.fileName = view.fileName
    document.sizeBytes = view.sizeBytes
    document.errorMessage = view.errorMessage
    document.updatedAt = view.updatedAt
    document.likeCount = view.likeCount
    document.dislikeCount = view.dislikeCount
    document.viewCount = view.viewCount
    document.embedding = embedding.toList()
    return document
}
