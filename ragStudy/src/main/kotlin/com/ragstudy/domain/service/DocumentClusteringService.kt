package com.ragstudy.domain.service

import org.springframework.stereotype.Service
import smile.clustering.KMeans
import com.ragstudy.global.util.ClusteringUtils
import kotlin.math.min

@Service
class DocumentClusteringService(private val embedService: EmbedService) {

    fun clusterDocuments(documents: List<String>, k: Int = 3): List<List<String>> {
        if (documents.isEmpty()) {
            return emptyList()
        }

        // 빈 문서 필터링
        val filteredDocuments = documents.filter { it.isNotBlank() }
        if (filteredDocuments.isEmpty()) {
            return emptyList()
        }

        // 텍스트를 임베딩으로 변환
        val embeddings = filteredDocuments.map { embedService.embedText(it) }

        // 실제 클러스터 수는 문서 수와 요청된 k 중 작은 값으로 설정
        val actualK = min(k, filteredDocuments.size)

        // FloatArray를 DoubleArray로 변환
        val data = embeddings.map { arr -> arr.map { v: Float -> v.toDouble() }.toDoubleArray() }.toTypedArray()

        // 클러스터 수 및 반복 설정
        val maxIter = 100
        val kmeans = KMeans.fit(data, actualK, maxIter)

        // 클러스터에 문서 매핑
        val clusters = Array(actualK) { mutableListOf<String>() }
        for ((i, doc) in filteredDocuments.withIndex()) {
            val clusterIndex = kmeans.predict(data[i])
            clusters[clusterIndex].add(doc)
        }

        // 클러스터 유사도 점수 계산 (선택적)
        val clusterScores = calculateClusterScores(clusters, embeddings)

        return clusters.toList()
    }

    private fun calculateClusterScores(
        clusters: Array<MutableList<String>>,
        embeddings: List<FloatArray>
    ): List<Double> {
        // 각 클러스터 내 문서들의 평균 유사도 점수 계산
        return clusters.mapIndexed { _, cluster ->
            if (cluster.size <= 1) return@mapIndexed 1.0 // 단일 문서 클러스터는 완벽한 유사도

            // 클러스터 내 모든 문서 쌍의 유사도 평균 계산
            var totalSimilarity = 0.0
            var pairCount = 0

            for (i in cluster.indices) {
                for (j in i + 1 until cluster.size) {
                    val vec1 = embeddings[i].map { it.toDouble() }
                    val vec2 = embeddings[j].map { it.toDouble() }
                    totalSimilarity += ClusteringUtils.calculateCosineSimilarity(vec1, vec2)
                    pairCount++
                }
            }

            if (pairCount > 0) totalSimilarity / pairCount else 1.0
        }
    }
}