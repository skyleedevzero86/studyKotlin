package com.sleekydz86.youtubedownload.global.elasticsearch

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch._types.ElasticsearchException
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorProperty
import com.sleekydz86.youtubedownload.global.embedding.ClusterVersionGate
import com.sleekydz86.youtubedownload.global.embedding.DenseVectorMapping
import com.sleekydz86.youtubedownload.global.embedding.IndexOptionRules
import com.sleekydz86.youtubedownload.global.env.StudyProperties
import com.sleekydz86.youtubedownload.global.error.DownloadException
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class ElasticsearchIndexAdmin(
    private val client: ElasticsearchClient,
    properties: StudyProperties,
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val elasticsearch = properties.elasticsearch

    @PostConstruct
    fun ensureIndex() {
        ClusterVersionGate.assertLaterVectorStack(clusterVersion())
        IndexOptionRules.validate(
            elasticsearch.dims,
            elasticsearch.indexType,
            elasticsearch.similarity,
            elasticsearch.m,
            elasticsearch.efConstruction,
        )
        try {
            openIndex()
        } catch (ex: DownloadException) {
            throw ex
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "인덱스 준비", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "인덱스 준비", ex)
        }
    }

    private fun openIndex() {
        if (indexExists()) {
            assertCompatible()
        } else {
            createIndex()
        }
        putReactionFields()
    }

    private fun indexExists(): Boolean =
        client.indices().exists { request -> request.index(elasticsearch.index) }.value()

    private fun createIndex() {
        val json = DenseVectorMapping.createIndexJson(
            elasticsearch.dims,
            elasticsearch.similarity,
            elasticsearch.indexType,
            elasticsearch.m,
            elasticsearch.efConstruction,
        )
        client.indices().create { request ->
            request.index(elasticsearch.index).withJson(json.byteInputStream())
        }
        log.info(
            "dense_vector 인덱스를 만들었습니다. index={} dims={} similarity={} indexType={}",
            elasticsearch.index,
            elasticsearch.dims,
            elasticsearch.similarity,
            elasticsearch.indexType,
        )
    }

    private fun assertCompatible() {
        val embedding = readEmbedding()
        requireSame(embedding.dims(), elasticsearch.dims, "dims")
        requireSame(embedding.similarity(), elasticsearch.similarity, "similarity")
        requireSame(embedding.indexOptions()?.type(), elasticsearch.indexType, "index_options.type")
        assertHnswParameters(embedding)
        log.info("기존 dense_vector 인덱스를 사용합니다. index={} dims={}", elasticsearch.index, elasticsearch.dims)
    }

    private fun putReactionFields() {
        client.indices().putMapping { request ->
            request.index(elasticsearch.index).withJson(DenseVectorMapping.reactionFieldsJson().byteInputStream())
        }
        log.info("반응 횟수 필드를 맞췄습니다. index={}", elasticsearch.index)
    }

    private fun assertHnswParameters(embedding: DenseVectorProperty) {
        if (!IndexOptionRules.usesHnsw(elasticsearch.indexType)) return
        requireSame(embedding.indexOptions()?.m(), elasticsearch.m, "m")
        requireSame(embedding.indexOptions()?.efConstruction(), elasticsearch.efConstruction, "ef_construction")
    }

    private fun readEmbedding(): DenseVectorProperty {
        val response = client.indices().getMapping { request -> request.index(elasticsearch.index) }
        val field = response.get(elasticsearch.index)?.mappings()?.properties()?.get("embedding")
        return field?.denseVector() ?: throw DownloadException(
            "인덱스에 embedding dense_vector 필드가 없습니다. index=${elasticsearch.index}",
            status = HttpStatus.CONFLICT,
        )
    }

    private fun requireSame(actual: Any?, expected: Any, label: String) {
        if (actual?.toString() == expected.toString()) return
        throw DownloadException(
            "이미 있는 인덱스의 $label 이 설정과 다릅니다. index=${elasticsearch.index}, 인덱스=$actual, 설정=$expected. " +
                "인덱스를 삭제한 뒤 다시 실행하세요. curl -X DELETE ${elasticsearch.url}/${elasticsearch.index}",
            status = HttpStatus.CONFLICT,
        )
    }

    private fun clusterVersion(): String =
        try {
            client.info().version().number()
        } catch (ex: IOException) {
            throw vectorFailure(elasticsearch, "버전 조회", ex)
        } catch (ex: ElasticsearchException) {
            throw vectorFailure(elasticsearch, "버전 조회", ex)
        }
}
