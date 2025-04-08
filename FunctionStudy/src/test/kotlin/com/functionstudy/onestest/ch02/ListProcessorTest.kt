package com.functionstudy.onestest.ch02

import com.functionstudy.ones.ch02.processor.ListProcessor
import com.functionstudy.ones.ch02.processor.andThen
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ListProcessorTest {
    @Test
    fun `데이터 처리 체이닝 테스트`() {
        // Given: 테스트용 데이터와 함수들 준비
        val processor = ListProcessor()
        val extractData = { "테스트 데이터" }
        val fetchContent = { data: String -> "처리된 $data" }
        val renderToHtml = { content: String -> "<html>$content</html>" }

        // 최종 결과를 저장할 변수
        var finalResult = ""
        val createResponse = { html: String -> finalResult = html }

        // When: 데이터 처리 수행
        processor.processListData(
            extractData,
            fetchContent,
            renderToHtml,
            createResponse
        )

        // Then: 결과 검증
        assertEquals("<html>처리된 테스트 데이터</html>", finalResult)
    }

    @Test
    fun `andThen 확장 함수 테스트`() {
        // Given: 두 개의 함수 준비
        val multiplyByTwo = { x: Int -> x * 2 }
        val addTen = { x: Int -> x + 10 }

        // When: andThen 확장 함수를 사용해 함수 결합
        val combinedFunction = multiplyByTwo.andThen(addTen)

        // Then: 결과 검증 (5 * 2 + 10 = 20)
        assertEquals(20, combinedFunction(5))
    }
}