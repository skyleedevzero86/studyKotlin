package com.sleekydz86.youtubedownload.global.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch._types.Refresh
import co.elastic.clients.elasticsearch._types.SortOrder
import co.elastic.clients.elasticsearch.core.search.Hit
import com.sleekydz86.youtubedownload.global.embedding.HashingEmbedder
import com.sleekydz86.youtubedownload.global.embedding.SearchLimits
import com.sleekydz86.youtubedownload.global.embedding.VectorCodec
import com.sleekydz86.youtubedownload.global.embedding.downloadText
import com.sleekydz86.youtubedownload.global.env.StudyProperties
import com.sleekydz86.youtubedownload.global.error.DownloadException
import com.sleekydz86.youtubedownload.video.DownloadSnapshot
import com.sleekydz86.youtubedownload.video.read.DownloadProjection
import com.sleekydz86.youtubedownload.video.read.DownloadSearch
import com.sleekydz86.youtubedownload.video.read.DownloadTextSearch
import com.sleekydz86.youtubedownload.video.read.DownloadVectorSearch
import com.sleekydz86.youtubedownload.video.read.IndexedPage
import com.sleekydz86.youtubedownload.video.read.PageQuery
import com.sleekydz86.youtubedownload.video.read.VectorHit
import com.sleekydz86.youtubedownload.video.read.wildcardPattern
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ElasticsearchDownloads(
    private val client: ElasticsearchClient,
    private val embedder: HashingEmbedder,
    properties: StudyProperties,
) : DownloadSearch, DownloadProjection, DownloadVectorSearch, DownloadTextSearch {
    private val elasticsearch = properties.elasticsearch

    override fun search(query: PageQuery): IndexedPage =
        try {
            val response = client.search({ request ->
                request.index(elasticsearch.index)
                    .from(query.page * query.size)
                    .size(query.size)
                    .sort { sort -> sort.field { field -> field.field("updatedAt").order(SortOrder.Desc) } }
                    .query { searchQuery -> searchQuery.matchAll { match -> match } }
            }, DownloadDocument::class.java)
            val total = response.hits().total()?.value() ?: 0
            IndexedPage(query.page, query.size, total, response.hits().hits().mapNotNull { indexedDownload(it.source()) })
        } catch (ex: DownloadException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "목록 조회", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "목록 조회", ex)
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

    override fun searchSaved(text: String, query: PageQuery): IndexedPage {
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
            val total = response.hits().total()?.value() ?: 0
            IndexedPage(query.page, query.size, total, response.hits().hits().mapNotNull { indexedDownload(it.source()) })
        } catch (ex: DownloadException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "저장 검색", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "저장 검색", ex)
        }
    }

    override fun upsert(snapshot: DownloadSnapshot) {
        try {
            client.index { request ->
                request.index(elasticsearch.index)
                    .id(snapshot.id.toString())
                    .document(downloadDocument(snapshot, vectorOf(snapshot)))
                    .refresh(Refresh.True)
            }
        } catch (ex: DownloadException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "색인 id=${snapshot.id}", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "색인 id=${snapshot.id}", ex)
        }
    }

    private fun vectorOf(snapshot: DownloadSnapshot): FloatArray {
        if (snapshot.embeddingJson.isNotBlank()) return VectorCodec.decode(snapshot.embeddingJson)
        return embedder.embed(downloadText(snapshot.videoId.value, snapshot.title, snapshot.fileName, snapshot.errorMessage))
    }

    private fun vectorHit(hit: Hit<Void>): VectorHit? {
        val id = hit.id()?.toLongOrNull() ?: return null
        return VectorHit(id, hit.score() ?: 0.0)
    }
}
