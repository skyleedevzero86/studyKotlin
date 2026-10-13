package com.sleekydz86.densevector.global.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch._types.Refresh
import com.sleekydz86.densevector.global.config.VectorProperties
import com.sleekydz86.densevector.global.error.VectorStudyException
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ElasticsearchPassageWriter(
    private val client: ElasticsearchClient,
    private val properties: VectorProperties
) : PassageVectorWriter {
    override fun upsert(id: Long, title: String, body: String, embedding: FloatArray) {
        try {
            client.index { request ->
                request.index(properties.indexName)
                    .id(id.toString())
                    .document(passageDocument(title, body, embedding))
                    .refresh(Refresh.True)
            }
        } catch (ex: IOException) {
            throw vectorFailure(properties, "색인 id=$id", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(properties, "색인 id=$id", ex)
        }
    }

    override fun bulkUpsert(items: List<IndexedPassage>) {
        if (items.isEmpty()) {
            return
        }
        try {
            items.chunked(200).forEach(::writeChunk)
            client.indices().refresh { request -> request.index(properties.indexName) }
        } catch (ex: VectorStudyException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(properties, "일괄 색인", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(properties, "일괄 색인", ex)
        }
    }

    override fun delete(id: Long) {
        try {
            client.delete { request ->
                request.index(properties.indexName).id(id.toString()).refresh(Refresh.True)
            }
        } catch (ex: IOException) {
            throw vectorFailure(properties, "삭제 id=$id", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(properties, "삭제 id=$id", ex)
        }
    }

    private fun writeChunk(items: List<IndexedPassage>) {
        val response = client.bulk { request ->
            items.forEach { item ->
                request.operations { operation ->
                    operation.index { index ->
                        index.index(properties.indexName)
                            .id(item.id.toString())
                            .document(passageDocument(item.title, item.body, item.embedding))
                    }
                }
            }
            request
        }
        if (response.errors()) {
            val reason = response.items().firstNotNullOfOrNull { it.error()?.reason() }
            throw vectorFailure(properties, "일괄 색인", IllegalStateException(reason))
        }
    }
}
