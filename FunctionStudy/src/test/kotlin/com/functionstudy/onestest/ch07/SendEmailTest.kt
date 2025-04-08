package com.functionstudy.onestest.ch07

import com.functionstudy.ones.ch07.domain.asFailure
import com.functionstudy.ones.ch07.domain.asSuccess
import com.functionstudy.ones.ch07.domain.sendEmail
import com.functionstudy.ones.ch07.failure.EmailError
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@DisplayName("이메일 전송 테스트")
class SendEmailTest {

    @Test
    @DisplayName("이메일 전송 성공")
    fun `이메일 전송 성공`() {
        // Given
        val fileName = "myfile.txt"

        // When
        val result = sendEmail(fileName)

        // Then

        println("이메일 전송 성공 결과: $result")
        expectThat(result).isEqualTo(Unit.asSuccess())
    }

    @Test
    @DisplayName("파일 읽기 실패로 이메일 전송 실패")
    fun `파일 읽기 실패로 이메일 전송 실패`() {
        // Given
        val fileName = "errorfile.txt"

        // When
        val result = sendEmail(fileName)

        // Then
        println("파일 읽기 실패 결과: $result")
        expectThat(result).isEqualTo(EmailError("error reading file errorfile.txt").asFailure())
    }

    @Test
    @DisplayName("이메일 전송 실패 (내용 오류)")
    fun `이메일 전송 실패 (내용 오류)`() {
        // Given
        val fileName = "error_email_content.txt"

        // When
        val result = sendEmail(fileName)

        // Then
        println("이메일 전송 실패 (내용 오류) 결과: $result")
        expectThat(result).isEqualTo(EmailError("이메일 전송 실패: 파일 내용: error_email_content.txt").asFailure())
    }
}
