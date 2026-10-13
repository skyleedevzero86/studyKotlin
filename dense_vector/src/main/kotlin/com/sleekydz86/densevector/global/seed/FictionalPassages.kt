package com.sleekydz86.densevector.global.seed

object FictionalPassages {
    private val topics = listOf(
        "코사인 유사도는 두 벡터의 방향이 비슷한 정도를 각도로 나타낸다.",
        "HNSW는 계층 그래프로 근사 최근접 이웃을 찾는다.",
        "dims는 dense_vector 한 칸의 차원 수이며 이 학습 데이터는 64차원이다.",
        "MySQL에는 원문을 두고 Elasticsearch에는 검색용 벡터를 둔다.",
        "int8_hnsw는 HNSW를 유지한 채 메모리를 줄이는 양자화이다."
    )

    fun title(sequence: Int): String = "가상 문단 ${sequence.toString().padStart(4, '0')}"

    fun body(sequence: Int): String {
        val topic = topics[(sequence - 1).floorMod(topics.size)]
        return "가상 문단 $sequence. $topic 개발용 예시이며 운영 데이터가 아니다."
    }

    private fun Int.floorMod(divisor: Int): Int = ((this % divisor) + divisor) % divisor
}
