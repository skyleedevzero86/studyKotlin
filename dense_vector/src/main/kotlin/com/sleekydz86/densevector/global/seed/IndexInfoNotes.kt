package com.sleekydz86.densevector.global.seed

object IndexInfoNotes {
    fun text(clusterVersion: String): String =
        "Elasticsearch $clusterVersion 의 dense_vector입니다. cosine의 _score는 (1 + cosine) / 2 입니다. " +
            "HNSW는 근사 최근접 이웃 검색입니다. 이 클러스터는 hnsw, int8_hnsw, int4_hnsw, flat, int8_flat, int4_flat을 쓰고 " +
            "bbq_hnsw, bbq_disk, vectordb_document는 지원하지 않습니다. 임베딩은 학습용 hashing-bigram이며 언어 모델이 아닙니다."
}
