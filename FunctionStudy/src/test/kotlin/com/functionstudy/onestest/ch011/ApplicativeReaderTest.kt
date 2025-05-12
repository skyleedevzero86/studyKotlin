package com.functionstudy.onestest.ch011

import com.functionstudy.ones.ch10.core.ContextReader
import com.functionstudy.ones.ch10.core.not
import com.functionstudy.ones.ch10.core.times
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import com.functionstudy.ones.ch10.core.curried

operator infix fun <CTX, A, B> ContextReader<CTX, (A) -> B>.times(
    reader: ContextReader<CTX, A>
): ContextReader<CTX, B> = this.flatMap { f -> reader.map(f) }

class ApplicativeReaderTest {

    private val numberReader = ContextReader<Map<String, String>, String> { ctx -> ctx["number"].orEmpty() }
    private val streetReader = ContextReader<Map<String, String>, String> { ctx -> ctx["street"].orEmpty() }
    private val cityReader = ContextReader<Map<String, String>, String> { ctx -> ctx["city"].orEmpty() }

    private fun address(number: String, street: String, city: String): String =
        "$number $street, $city"

    private val sampleConfig = mapOf("number" to "10", "street" to "다우닝 스트리트", "city" to "런던")

    @Test
    @DisplayName("설정 값을 읽고 올바르게 적용해야 한다")
    fun shouldApplyConfigurationValuesCorrectly() {
        val curriedAddress = ::address.curried()
        val liftedFunction = curriedAddress not numberReader
        val intermediateResult = liftedFunction * streetReader
        val result = intermediateResult * cityReader // Applies to cityReader
        expectThat(result.runWith(sampleConfig)).isEqualTo("10 다우닝 스트리트, 런던")
    }

    @Test
    @DisplayName("3개의 인자를 받는 함수에 애플리케이티브를 적용할 수 있어야 한다")
    fun shouldApplyApplicativeForThreeArgumentsFunction() {
        fun welcomeMessage(name: String, company: String, role: String): String =
            "${name}님, ${company}의 ${role}으로 환영합니다!"

        val nameReader = ContextReader<Map<String, String>, String> { ctx -> ctx["name"].orEmpty() }
        val companyReader = ContextReader<Map<String, String>, String> { ctx -> ctx["company"].orEmpty() }
        val roleReader = ContextReader<Map<String, String>, String> { ctx -> ctx["role"].orEmpty() }

        val welcomeConfig = mapOf(
            "name" to "홍길동",
            "company" to "함수형주식회사",
            "role" to "시니어 개발자"
        )

        val curriedWelcome = ::welcomeMessage.curried()
        val liftedFunction = curriedWelcome not nameReader
        val intermediateResult = liftedFunction * companyReader
        val messageReader = intermediateResult * roleReader
        expectThat(messageReader.runWith(welcomeConfig))
            .isEqualTo("홍길동님, 함수형주식회사의 시니어 개발자로 환영합니다!")
    }
}