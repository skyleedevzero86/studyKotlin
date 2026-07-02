package com.kochat.global.application.admin

import com.kochat.adapter.inbound.web.survey.dto.SurveyStatisticsDto
import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.FontFactory
import com.lowagie.text.PageSize
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class SurveyExportService {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

    fun exportExcel(data: SurveyStatisticsDto): ByteArray {
        val workbook = XSSFWorkbook()
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            setFont(workbook.createFont().apply { bold = true })
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
        val dataStyle = workbook.createCellStyle().apply {
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        val summarySheet = workbook.createSheet("요약")
        var rowIdx = 0
        summarySheet.createRow(rowIdx++).apply {
            createCell(0).setCellValue("설문 제목")
            createCell(1).setCellValue(data.title)
        }
        summarySheet.createRow(rowIdx++).apply {
            createCell(0).setCellValue("참여자")
            createCell(1).setCellValue("${data.completedParticipants} / ${data.totalParticipants}")
        }
        summarySheet.createRow(rowIdx++).apply {
            createCell(0).setCellValue("보낸 시각")
            createCell(1).setCellValue(LocalDateTime.now().format(dateTimeFormatter))
        }

        val questionSheet = workbook.createSheet("문항별")
        var qRow = 0
        questionSheet.createRow(qRow++).apply {
            listOf("문항번호", "문항", "보기/답변", "응답수").forEachIndexed { i, h ->
                createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
        }
        data.byQuestion.forEach { question ->
            if (question.options.isNotEmpty()) {
                question.options.forEach { option ->
                    questionSheet.createRow(qRow++).apply {
                        createCell(0).setCellValue(question.questionNo.toDouble())
                        createCell(1).setCellValue(question.questionText)
                        createCell(2).setCellValue(option.optionText)
                        createCell(3).setCellValue((option.selectCount ?: 0L).toDouble())
                        (0..3).forEach { createCell(it).cellStyle = dataStyle }
                    }
                }
            } else {
                question.textAnswers.forEach { answer ->
                    questionSheet.createRow(qRow++).apply {
                        createCell(0).setCellValue(question.questionNo.toDouble())
                        createCell(1).setCellValue(question.questionText)
                        createCell(2).setCellValue(answer)
                        createCell(3).setCellValue(1.0)
                        (0..3).forEach { createCell(it).cellStyle = dataStyle }
                    }
                }
            }
        }

        val participantSheet = workbook.createSheet("참여자별")
        var pRow = 0
        participantSheet.createRow(pRow++).apply {
            listOf("사용자", "상태", "문항", "응답").forEachIndexed { i, h ->
                createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
        }
        data.byParticipant.forEach { participant ->
            val name = participant.displayName ?: participant.username
            val status = if (participant.status.name == "COMPLETED") "완료" else "대기"
            participant.answers.forEach { answer ->
                val response = answer.textAnswer ?: answer.optionTexts.joinToString(", ")
                participantSheet.createRow(pRow++).apply {
                    createCell(0).setCellValue(name)
                    createCell(1).setCellValue(status)
                    createCell(2).setCellValue(answer.questionText)
                    createCell(3).setCellValue(response)
                    (0..3).forEach { createCell(it).cellStyle = dataStyle }
                }
            }
        }

        listOf(summarySheet, questionSheet, participantSheet).forEach { sheet ->
            (0 until 4).forEach { sheet.autoSizeColumn(it) }
        }

        return ByteArrayOutputStream().use { out ->
            workbook.write(out)
            workbook.close()
            out.toByteArray()
        }
    }

    fun exportPdf(data: SurveyStatisticsDto): ByteArray {
        val document = Document(PageSize.A4.rotate())
        val out = ByteArrayOutputStream()
        PdfWriter.getInstance(document, out)
        document.open()

        val baseFont = BaseFont.createFont("HYGoThic-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED)
        val titleFont = Font(baseFont, 14f, Font.BOLD)
        val headerFont = Font(baseFont, 10f, Font.BOLD)
        val bodyFont = Font(baseFont, 9f)

        document.add(Phrase("설문 통계: ${data.title}", titleFont))
        document.add(Phrase("참여 ${data.completedParticipants}/${data.totalParticipants}", bodyFont))
        document.add(Phrase(" "))

        document.add(Phrase("문항별 통계", headerFont))
        data.byQuestion.forEach { question ->
            document.add(Phrase("${question.questionNo}. ${question.questionText}", bodyFont))
            if (question.options.isNotEmpty()) {
                question.options.forEach { option ->
                    document.add(Phrase("  - ${option.optionText}: ${option.selectCount ?: 0}", bodyFont))
                }
            } else {
                question.textAnswers.forEach { answer ->
                    document.add(Phrase("  - $answer", bodyFont))
                }
            }
        }

        document.add(Phrase(" "))
        document.add(Phrase("참여자별 응답", headerFont))
        val table = PdfPTable(4).apply { widthPercentage = 100f }
        listOf("사용자", "상태", "문항", "응답").forEach { header ->
            table.addCell(PdfPCell(Phrase(header, headerFont)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
            })
        }
        data.byParticipant.forEach { participant ->
            val name = participant.displayName ?: participant.username
            val status = if (participant.status.name == "COMPLETED") "완료" else "대기"
            participant.answers.forEach { answer ->
                val response = answer.textAnswer ?: answer.optionTexts.joinToString(", ")
                table.addCell(Phrase(name, bodyFont))
                table.addCell(Phrase(status, bodyFont))
                table.addCell(Phrase(answer.questionText, bodyFont))
                table.addCell(Phrase(response, bodyFont))
            }
        }
        document.add(table)
        document.close()
        return out.toByteArray()
    }
}
