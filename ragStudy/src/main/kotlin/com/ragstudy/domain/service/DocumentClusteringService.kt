package com.ragstudy.domain.service

import org.springframework.stereotype.Service
import smile.clustering.KMeans

@Service
class DocumentClusteringService(private val embedService: EmbedService) {

    fun clusterDocuments(documents: List<String>): List<List<String>> {
        // 텍스트를 임베딩으로 변환
        val embeddings = documents.map { embedService.embedText(it) }

        // FloatArray를 DoubleArray로 변환
        val data = embeddings.map { arr -> arr.map { v: Float -> v.toDouble() }.toDoubleArray() }.toTypedArray()

        // 클러스터 수 및 반복 설정
        val k = 3
        val maxIter = 100
        val kmeans = KMeans.fit(data, k, maxIter)

        // 클러스터에 문서 매핑
        val clusters = Array(k) { mutableListOf<String>() }
        for ((i, doc) in documents.withIndex()) {
            val clusterIndex = kmeans.predict(data[i])
            clusters[clusterIndex].add(doc)
        }

        return clusters.toList()
    }
}