package com.functionstudy.onestest.ch04

import com.functionstudy.ones.ch04.template.EmailTemplate
import com.functionstudy.ones.ch04.template.Person
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import com.functionstudy.ones.ch03.utils.StringTag
import com.functionstudy.ones.ch03.utils.renderTemplate
import com.functionstudy.ones.ch03.utils.expectThat

class EmailTemplateTest {
    @Test
    fun testEmailTemplate() {
        // Given: 초기 데이터 설정
        val john = Person("John")
        val emailTemplate = EmailTemplate("Hello, {{name}}! Welcome aboard.")

        // When: 이메일 템플릿을 적용하여 개인화된 이메일 생성
        val personalizedEmail = emailTemplate.invoke(john)

        // Then: 결과가 예상한대로 맞는지 확인
        println("Generated Email: $personalizedEmail")
        assertEquals("Hello, John! Welcome aboard.", personalizedEmail)
    }

    @Test
    fun `이메일 템플릿 테스트2`() {
        // given: 템플릿 문자열과 데이터를 준비
        val template = "Hello, {name}! Welcome aboard."
        val john = mapOf("name" to StringTag("John"))

        // when: 템플릿을 렌더링하여 개인화된 이메일 생성
        val personalizedEmail = renderTemplate(template, john)

        // then: 결과가 예상한대로 맞는지 확인
        println("Generated Email: $personalizedEmail")
        expectThat(personalizedEmail).isEqualTo("Hello, John! Welcome aboard.")
    }
}