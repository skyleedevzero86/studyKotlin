package com.sleekydz86.cdcmigration.global.csv

import com.sleekydz86.cdcmigration.global.error.BusinessException
import com.sleekydz86.cdcmigration.global.error.ErrorCode
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.springframework.stereotype.Component
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Component
class CsvTableReader {

    fun read(input: InputStream, fileLabel: String, requiredHeaders: List<String>): List<Map<String, String?>> {
        val format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreEmptyLines(true)
            .setTrim(false)
            .get()
        try {
            InputStreamReader(stripBom(input), StandardCharsets.UTF_8).use { reader ->
                CSVParser.parse(reader, format).use { parser ->
                    val headers = parser.headerNames.map { it.trim() }
                    val missing = requiredHeaders.filterNot { it in headers }
                    if (missing.isNotEmpty()) {
                        throw BusinessException(ErrorCode.CSV_FORMAT, "$fileLabel: 필요한 열이 없습니다 (${missing.joinToString(", ")})")
                    }
                    return parser.records.map { record ->
                        headers.associateWith { header ->
                            val value = if (record.isMapped(header)) record.get(header) else null
                            if (value.isNullOrEmpty()) null else value
                        }
                    }
                }
            }
        } catch (exception: BusinessException) {
            throw exception
        } catch (exception: Exception) {
            throw BusinessException(ErrorCode.CSV_FORMAT, "$fileLabel: ${exception.message}", exception)
        }
    }

    private fun stripBom(input: InputStream): InputStream {
        val buffered = input.buffered()
        buffered.mark(3)
        val head = ByteArray(3)
        val read = buffered.read(head)
        if (read == 3 && head[0] == 0xEF.toByte() && head[1] == 0xBB.toByte() && head[2] == 0xBF.toByte()) {
            return buffered
        }
        buffered.reset()
        return buffered
    }
}
