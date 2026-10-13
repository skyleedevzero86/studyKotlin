package com.sleekydz86.densevector2.global.seed

object SamplePassages {
    val texts: List<Pair<String, String>> = listOf(
        "코사인 유사도" to "코사인 유사도는 두 벡터의 방향이 얼마나 비슷한지 각도로 재는 방법이다. 길이가 1인 벡터에서는 코사인이 내적과 같다.",
        "HNSW 인덱싱" to "HNSW는 계층적 탐색 가능 작은 세계 그래프를 만들어 근사 최근접 이웃을 빠르게 찾는다. m과 ef_construction이 그래프의 연결과 정확도를 조절한다.",
        "벡터 차원 수" to "dense_vector의 dims는 벡터 차원 수이다. 차원 수는 임베딩 길이와 같아야 하며 4096을 넘길 수 없다.",
        "MySQL과 Elasticsearch 역할" to "MySQL에는 원문과 임베딩 배열을 저장한다. Elasticsearch dense_vector 인덱스는 코사인 유사도 기반 kNN 검색을 담당한다.",
        "양자화와 메모리" to "bbq_hnsw는 이진 양자화와 HNSW를 같이 쓴다. bbq_disk는 양자화 벡터를 디스크에 둔다. 이 프로젝트의 인덱스는 vectordb_document 모드다.",
        "날씨 메모" to "오늘은 맑고 바람이 약하다. 이 문장은 벡터 검색과 관련 없는 대조 예시다."
    )
}
