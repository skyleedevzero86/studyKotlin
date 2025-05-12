package com.functionstudy.ones.ch07.failure

import com.functionstudy.ones.ch07.domain.*
import com.functionstudy.ones.ch07.inter.OutcomeError


class EmailError(override val msg: String) : OutcomeError

class FileReadError(override val msg: String) : OutcomeError

object FileReader {
    fun readFile(fileName: String): Outcome<FileReadError, String> {
        return if (fileName == "errorfile.txt") {
            Outcome.Failure(FileReadError("파일을 읽을 수 없습니다: $fileName"))
        } else {
            Outcome.Success("파일 내용: $fileName")
        }
    }
}
