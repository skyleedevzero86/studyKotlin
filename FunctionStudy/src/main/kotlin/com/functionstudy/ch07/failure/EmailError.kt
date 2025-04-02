package com.functionstudy.ch07.failure

import com.functionstudy.ch07.domain.*
import com.functionstudy.ch07.inter.OutcomeError

class EmailError(val msg: String) : OutcomeError

object FileReader {
    fun readFile(fileName: String): Outcome<FileReadError, String> {
        return if (fileName == "errorfile.txt") {
            Failure(FileReadError("파일을 읽을 수 없습니다: $fileName"))
        } else {
            Success("파일 내용: $fileName")
        }
    }
}

class FileReadError(val msg: String) : OutcomeError
