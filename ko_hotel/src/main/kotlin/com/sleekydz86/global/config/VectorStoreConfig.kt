package com.sleekydz86.global.config

import jakarta.annotation.PostConstruct
import org.springframework.ai.document.Document
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.annotation.Qualifier

@Configuration
class VectorStoreConfig {

    @Bean
    fun vectorStore(@Qualifier("customEmbeddingModel") embeddingModel: EmbeddingModel): VectorStore {
        val vectorStore = SimpleVectorStore.builder(CachedEmbeddingModel(embeddingModel)).build()
        initializeVectorStore(vectorStore)
        return vectorStore
    }

    private fun initializeVectorStore(vectorStore: VectorStore) {
        try {
            val hotelDocuments = createHotelDocuments()
            vectorStore.add(hotelDocuments)
            println("호텔 정보가 벡터 저장소에 추가되었습니다: ${hotelDocuments.size}개 문서")
            if (hotelDocuments.isNotEmpty()) {
                println("샘플 문서: ${hotelDocuments[0].text}")
            }
        } catch (e: Exception) {
            println("벡터 저장소 초기화 오류: ${e.message}")
        }
    }

    private fun createHotelDocuments(): List<Document> {
        return listOf(
            Document(
                """
                궁금하면 500원 환영 인사: 
                손님이 호텔에 도착하면 친절한 미소와 함께 '어서 오세요, 주잉님!' 같은 개인화된 인사말을 사용합니다. 
                재방문 고객에게는 특별한 감사 인사를 전달합니다.
                """.trimIndent(),
                mapOf("category" to "greeting", "source" to "hotel_policy", "priority" to "1")
            ),
            Document(
                """
                체크인/체크아웃 시간:
                체크인 시간은 오후 3시, 체크아웃 시간은 오전 11시입니다. 
                이른 체크인이나 늦은 체크아웃 요청 시 객실 공실 상태를 확인하고, 불가능하면 짐 보관 서비스를 제공합니다. 
                체크아웃 연장 시 추가 요금이 부과될 수 있습니다.
                """.trimIndent(),
                mapOf("category" to "checkin_checkout", "source" to "hotel_policy", "priority" to "1")
            ),
            Document(
                """
                Wi-Fi 및 주차장 정보:
                모든 객실에는 무료 Wi-Fi가 제공됩니다. Wi-Fi 이름과 비밀번호를 안내하며, 연결 문제 시 해결 방법을 제공합니다. 
                무료 주차장은 200대 수용 가능, 24시간 운영됩니다.
                """.trimIndent(),
                mapOf("category" to "facilities", "source" to "hotel_amenities", "priority" to "2")
            ),
            Document(
                """
                유니버설 룸 및 장애인 시설:
                유니버설 룸은 휠체어 사용 고객을 위해 샤워 체어, 높이 조절 침대 등을 갖추고 있습니다. 
                엘리베이터, 장애인 화장실, 경사로, 휠체어 대여 서비스를 제공합니다.
                """.trimIndent(),
                mapOf("category" to "accessibility", "source" to "hotel_amenities", "priority" to "2")
            ),
            Document(
                """
                반려동물 정책:
                반려동물 동반은 불가능합니다. 인근 반려동물 호텔 정보를 제공합니다.
                """.trimIndent(),
                mapOf("category" to "pet_policy", "source" to "hotel_policy", "priority" to "2")
            ),
            Document(
                """
                룸 서비스:
                룸 서비스는 오전 7시부터 오후 11시까지 운영됩니다. 
                알레르기나 특별 식단 요청 시 주방과 협력합니다.
                """.trimIndent(),
                mapOf("category" to "room_service", "source" to "hotel_services", "priority" to "2")
            ),
            Document(
                """
                금연 정책:
                모든 객실은 금연이며, 1층 흡연실(24시간 운영)을 안내합니다. 
                객실 내 흡연 시 청소 비용 30,000원이 부과됩니다.
                """.trimIndent(),
                mapOf("category" to "smoking_policy", "source" to "hotel_policy", "priority" to "2")
            ),
            Document(
                """
                취소 정책 및 수수료:
                취소 수수료: 전날 30%, 당일 50%, 연락 없이 100%. 
                특별 사정 시 관리자 승인 하에 유연 대응.
                """.trimIndent(),
                mapOf("category" to "cancellation", "source" to "hotel_policy", "priority" to "1")
            ),
            Document(
                """
                결제 방법:
                결제는 현금, 신용카드, 직불카드 가능. 카드 환불은 3~5일 소요.
                """.trimIndent(),
                mapOf("category" to "payment", "source" to "hotel_policy", "priority" to "2")
            ),
            Document(
                """
                고객 서비스:
                고객 불만은 신속히 처리하고, 사과 및 해결책을 제시합니다.
                """.trimIndent(),
                mapOf("category" to "customer_service", "source" to "hotel_policy", "priority" to "1")
            ),
            Document(
                """
                응급 상황 대응:
                화재 시 대피 경로와 소화기 위치를 안내하며, 응급 의료 상황 시 119 호출 및 병원 연락처 제공.
                """.trimIndent(),
                mapOf("category" to "emergency", "source" to "hotel_safety", "priority" to "1")
            ),
            Document(
                """
                관광 정보 서비스:
                주변 관광지, 레스토랑, 쇼핑몰 정보와 지역 투어 예약 서비스를 제공합니다.
                """.trimIndent(),
                mapOf("category" to "tourism", "source" to "hotel_services", "priority" to "3")
            )
        )
    }
}