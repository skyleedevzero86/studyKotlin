package com.kominioai.domain.survey.adapter.out.monitoring

import com.kominioai.domain.survey.application.dto.SurveyResult
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

@Component
class ExcelExportService {
    fun generateSurveyResultsExcel(results: List<SurveyResult>): ByteArray {
        val wb: Workbook = SXSSFWorkbook()
        val sheet = wb.createSheet("Results")
        val header = sheet.createRow(0)
        header.createCell(0).setCellValue("질문번호")
        header.createCell(1).setCellValue("질문내용")
        header.createCell(2).setCellValue("응답")

        results.forEachIndexed { idx, result ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(result.questionOrder.toDouble())
            row.createCell(1).setCellValue(result.questionContent)
            row.createCell(2).setCellValue(result.answer)
        }
        return wb.use { w ->
            ByteArrayOutputStream().use { out ->
                w.write(out)
                out.toByteArray()
            }
        }
    }
}