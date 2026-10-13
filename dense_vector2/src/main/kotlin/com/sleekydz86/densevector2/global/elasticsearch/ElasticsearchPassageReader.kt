package com.sleekydz86.densevector2.global.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch.core.search.Hit
import com.sleekydz86.densevector2.global.config.VectorProperties
import com.sleekydz86.densevector2.global.embedding.SearchLimits
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ElasticsearchPassageReader(
    private val client: ElasticsearchClient,
    private val properties: VectorProperties
) : PassageVectorReader {
    override fun search(query: FloatArray, k: Int): List<VectorHit> {
        val candidates = SearchLimits.numCandidates(k, properties.numCandidates)
        try {
            val response = client.search({ request ->
                request.index(properties.indexName)
                    .knn { knn ->
                        knn.field("embedding")
                            .queryVector(query.toList())
                            .k(k)
                            .numCandidates(candidates)
                    }
                    .source { source -> source.fetch(false) }
            }, Void::class.java)
            return response.hits().hits().mapNotNull(::toHit)
        } catch (ex: IOException) {
            throw vectorFailure(properties, "kNN 검색", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(properties, "kNN 검색", ex)
        }
    }

    private fun toHit(hit: Hit<Void>): VectorHit? {
        val id = hit.id()?.toLongOrNull() ?: return null
        return VectorHit(id, hit.score() ?: 0.0)
    }
}
