package com.functionstudy.ch07.domain

import com.functionstudy.ch07.failure.EmailError
import com.functionstudy.ch07.failure.FileReader


object EmailSender {
    fun sendTextByEmail(content: String): Outcome<EmailError, Unit> {
        return if (content.contains("error")) {
            Failure(EmailError("이메일 전송 실패: $content"))
        } else {
            Success(Unit)
        }
    }
}
fun sendEmail(fileName: String): Outcome<EmailError, Unit> =
    FileReader.readFile(fileName)
        .transformFailure { EmailError("파일 읽기 오류: ${it.msg}") }
        .onFailure { return Failure(it) }
        .let { EmailSender.sendTextByEmail(it) }
