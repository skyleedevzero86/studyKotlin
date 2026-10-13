package com.sleekydz86.densevector.global.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorProperty
import com.sleekydz86.densevector.global.config.VectorProperties
import com.sleekydz86.densevector.global.embedding.DenseVectorMapping
import com.sleekydz86.densevector.global.embedding.IndexOptionRules
import com.sleekydz86.densevector.global.error.VectorStudyException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ElasticsearchIndexAdmin(
    private val client: ElasticsearchClient,
    private val properties: VectorProperties
) : VectorIndexAdmin {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun ensureIndex() {
        IndexOptionRules.validate(
            properties.dims,
            properties.indexType,
            properties.similarity,
            properties.m,
            properties.efConstruction
        )
        try {
            openIndex()
        } catch (ex: VectorStudyException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(properties, "인덱스 준비", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(properties, "인덱스 준비", ex)
        }
    }

    override fun describe(): IndexDescription = IndexDescription(
        index = properties.indexName,
        dims = properties.dims,
        similarity = properties.similarity,
        indexType = properties.indexType,
        m = properties.m,
        efConstruction = properties.efConstruction,
        clusterVersion = clusterVersion()
    )

    private fun openIndex() {
        if (indexExists()) {
            assertCompatible()
            return
        }
        createIndex()
    }

    private fun indexExists(): Boolean =
        client.indices().exists { request -> request.index(properties.indexName) }.value()

    private fun createIndex() {
        val json = DenseVectorMapping.createIndexJson(
            properties.dims,
            properties.similarity,
            properties.indexType,
            properties.m,
            properties.efConstruction
        )
        client.indices().create { request ->
            request.index(properties.indexName).withJson(json.byteInputStream())
        }
        log.info(
            "dense_vector 인덱스 생성 index={} dims={} similarity={} indexType={}",
            properties.indexName,
            properties.dims,
            properties.similarity,
            properties.indexType
        )
    }

    private fun assertCompatible() {
        val embedding = readEmbedding()
        requireSame(embedding.dims(), properties.dims, "dims")
        requireSame(embedding.similarity(), properties.similarity, "similarity")
        requireSame(embedding.indexOptions()?.type(), properties.indexType, "index_options.type")
        assertHnswParameters(embedding)
        log.info("기존 dense_vector 인덱스를 사용합니다. index={} dims={}", properties.indexName, properties.dims)
    }

    private fun assertHnswParameters(embedding: DenseVectorProperty) {
        if (!IndexOptionRules.usesHnsw(properties.indexType)) {
            return
        }
        requireSame(embedding.indexOptions()?.m(), properties.m, "m")
        requireSame(embedding.indexOptions()?.efConstruction(), properties.efConstruction, "ef_construction")
    }

    private fun readEmbedding(): DenseVectorProperty {
        val response = client.indices().get { request -> request.index(properties.indexName) }
        val field = response.get(properties.indexName)?.mappings()?.properties()?.get("embedding")
        return field?.denseVector() ?: throw VectorStudyException(
            HttpStatus.CONFLICT,
            "인덱스에 embedding dense_vector 필드가 없습니다. index=${properties.indexName}"
        )
    }

    private fun requireSame(actual: Any?, expected: Any, label: String) {
        if (actual?.toString() == expected.toString()) {
            return
        }
        throw VectorStudyException(
            HttpStatus.CONFLICT,
            "이미 있는 인덱스의 $label 이 설정과 다릅니다. index=${properties.indexName}, 인덱스=$actual, 설정=$expected. " +
                "인덱스를 삭제한 뒤 다시 실행하세요. curl -X DELETE ${properties.elasticsearchUrl}/${properties.indexName}"
        )
    }

    private fun clusterVersion(): String {
        try {
            return client.info().version().number()
        } catch (ex: IOException) {
            throw vectorFailure(properties, "버전 조회", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(properties, "버전 조회", ex)
        }
    }
}
