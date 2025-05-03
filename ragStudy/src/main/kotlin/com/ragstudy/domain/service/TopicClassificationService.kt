package com.ragstudy.domain.service

import org.springframework.stereotype.Service
import com.ragstudy.global.util.TextUtils

@Service
class TopicClassificationService {

    // 사전 정의된 주제별 키워드 (실제 사용 시 더 확장 필요)
    private val topicKeywords = mapOf(
        "기술" to listOf("기술", "테크", "ai", "기계학습", "머신러닝", "인공지능", "컴퓨터", "소프트웨어", "하드웨어", "앱", "어플리케이션", "프로그래밍", "코딩", "개발", "it", "보안", "네트워크", "클라우드", "데이터", "분석"),
        "경제" to listOf("경제", "금융", "주식", "투자", "시장", "은행", "펀드", "돈", "화폐", "물가", "인플레이션", "디플레이션", "재정", "예산", "비용", "수익", "적자", "흑자", "경기", "불황", "호황"),
        "정치" to listOf("정치", "정부", "국회", "의원", "대통령", "선거", "투표", "후보", "당", "여당", "야당", "정책", "법안", "법률", "헌법", "민주주의", "독재", "외교", "국제", "국내"),
        "사회" to listOf("사회", "문화", "예술", "교육", "학교", "대학", "복지", "의료", "건강", "환경", "인권", "차별", "평등", "불평등", "고용", "취업", "실업", "임금", "노동", "노조"),
        "스포츠" to listOf("스포츠", "운동", "경기", "선수", "팀", "축구", "야구", "농구", "배구", "테니스", "골프", "올림픽", "월드컵", "챔피언", "메달", "우승", "준우승", "경쟁", "트레이닝"),
        "엔터테인먼트" to listOf("연예", "엔터테인먼트", "영화", "드라마", "음악", "노래", "가수", "배우", "공연", "콘서트", "축제", "예능", "방송", "채널", "프로그램", "시리즈", "게임", "애니메이션")
    )

    fun classifyTopic(text: String): Map<String, Double> {
        if (text.isBlank()) {
            return emptyMap()
        }

        val normalizedText = TextUtils.normalize(text)
        val tokens = TextUtils.tokenize(normalizedText)

        // 각 주제별 점수 계산
        val scores = mutableMapOf<String, Double>()

        topicKeywords.forEach { (topic, keywords) ->
            // 텍스트에 해당 주제 키워드가 얼마나 포함되어 있는지 계산
            var matchCount = 0
            keywords.forEach { keyword ->
                if (tokens.any { it.contains(keyword) }) {
                    matchCount++
                }
            }

            // 포함된 비율 계산
            val score = matchCount.toDouble() / keywords.size
            scores[topic] = score
        }

        // 점수 정규화 (전체 합이 1.0이 되도록)
        val totalScore = scores.values.sum().takeIf { it > 0 } ?: 1.0

        return scores.mapValues { it.value / totalScore }
    }
}