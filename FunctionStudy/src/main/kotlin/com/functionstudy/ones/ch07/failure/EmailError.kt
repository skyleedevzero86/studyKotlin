package com.functionstudy.ones.ch07.failure

import com.functionstudy.ones.ch07.domain.*
import com.functionstudy.ones.ch07.inter.OutcomeError

// OutcomeError를 상속받은 EmailError 클래스
class EmailError(override val msg: String) : OutcomeError

// OutcomeError를 상속받은 FileReadError 클래스
class FileReadError(override val msg: String) : OutcomeError

// 파일 읽기 작업을 수행하는 FileReader 객체
object FileReader {
    fun readFile(fileName: String): Outcome<FileReadError, String> {
        return if (fileName == "errorfile.txt") {
            Outcome.Failure(FileReadError("파일을 읽을 수 없습니다: $fileName"))
        } else {
            Outcome.Success("파일 내용: $fileName")
        }
    }
}
