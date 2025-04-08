package com.functionstudy.ones.ch04.controller

import com.functionstudy.ones.ch04.template.EmailTemplate
import com.functionstudy.ones.ch04.template.Person
import com.functionstudy.ones.ch03.utils.expectThat

class EmailTemplateExampleController {
    fun execute() {
        val john = Person("John")
        val emailTemplate = EmailTemplate("Hello, {{name}}! Welcome aboard.")

        val personalizedEmail = emailTemplate.invoke(john)
        println("개인화된 이메일: $personalizedEmail")

        // 예상되는 값과 비교
        val expectedEmail = "Hello, John! Welcome aboard."

        expectThat(personalizedEmail).isEqualTo(expectedEmail)

        println("이메일 템플릿이 예상과 일치합니다!")
    }
}

fun main() {
    val controller = EmailTemplateExampleController()
    controller.execute()
}
