package com.sleekydz86.cdcmigration.global

import com.sleekydz86.cdcmigration.global.csv.CsvTableReader
import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CSV 입력 어댑터")
class CsvTableReaderTest {

    private val reader = CsvTableReader()

    @Test
    @DisplayName("UTF-8 BOM이 있어도 첫 열 이름을 읽고, 빈 값은 null로 바꾼다")
    fun readsBomAndBlank() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "inst_code,user_id,user_name\nH01,u1001,\n".toByteArray()

        val rows = reader.read(bytes.inputStream(), "users.csv", listOf("inst_code", "user_id", "user_name"))

        assertThat(rows).hasSize(1)
        assertThat(rows[0]["inst_code"]).isEqualTo("H01")
        assertThat(rows[0]["user_name"]).isNull()
    }

    @Test
    @DisplayName("필요한 열이 없으면 한국어 사유로 거부한다")
    fun rejectsMissingHeader() {
        val bytes = "inst_code,user_id\nH01,u1001\n".toByteArray()

        assertThatThrownBy { reader.read(bytes.inputStream(), "users.csv", listOf("inst_code", "user_id", "user_name")) }
            .isInstanceOfSatisfying(BusinessException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.CSV_FORMAT)
                assertThat(it.detail).contains("필요한 열이 없습니다").contains("user_name")
            }
    }
}
