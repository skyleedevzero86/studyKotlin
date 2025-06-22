package com.sleekydz86.health.service

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.sleekydz86.health.entity.HealthLog
import com.sleekydz86.health.entity.User
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream

@Service
class HealthReportService {

    fun generateReport(user: User, healthLogs: List<HealthLog>, period: String): ByteArray {
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter(baos)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc)

        // 제목
        document.add(Paragraph("${user.userNm} (${user.userId})님의 건강 리포트")
            .setBold()
            .setFontSize(18f))

        // 기간 정보
        document.add(Paragraph("기간: $period")
            .setFontSize(12f))

        // 건강 데이터 테이블
        val table = Table(7)
        table.addCell("로그 ID")
        table.addCell("수면 시간")
        table.addCell("걸음 수")
        table.addCell("스트레스 수치")
        table.addCell("심박수")
        table.addCell("기록 날짜")
        table.addCell("경고")

        healthLogs.forEach { log ->
            table.addCell(log.logId.toString())
            table.addCell(log.sleepHours.toString())
            table.addCell(log.steps.toString())
            table.addCell(log.stressLevel.toString())
            table.addCell(log.heartRate.toString())
            table.addCell(log.logDate.toString())
            table.addCell(log.warning.toString())
        }
        document.add(table as com.itextpdf.layout.element.IBlockElement)

        document.close()
        return baos.toByteArray()
    }
}