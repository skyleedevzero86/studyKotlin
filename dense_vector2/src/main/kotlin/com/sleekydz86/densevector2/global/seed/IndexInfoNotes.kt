package com.sleekydz86.densevector2.global.seed

object IndexInfoNotes {
    fun text(clusterVersion: String): String =
        "Elasticsearch $clusterVersion 의 vectordb_document 인덱스입니다. cosine의 _score는 (1 + cosine) / 2 입니다. " +
            "bbq_hnsw는 9.1부터, bbq_disk는 9.4 Enterprise부터, vectordb_document는 9.5부터입니다. " +
            "8.15 클러스터에서는 이 기능을 쓰지 않습니다. 임베딩은 학습용 hashing-bigram이며 언어 모델이 아닙니다."
}
