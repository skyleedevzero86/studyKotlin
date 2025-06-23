package com.sleekydz86.health.service

import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.sleekydz86.health.entity.HealthLog
import com.sleekydz86.health.entity.User
import org.jfree.chart.ChartFactory
import org.jfree.chart.JFreeChart
import org.jfree.data.category.DefaultCategoryDataset
import org.springframework.stereotype.Service
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO

@Service
class HealthReportService(
    private val sentimentAnalysisService: SentimentAnalysisService
) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시")

    fun generateReport(user: User, healthLogs: List<HealthLog>, period: String): ByteArray {
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter(baos)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc).apply {
            setMargins(20f, 20f, 20f, 20f)
        }

        // 한글 폰트 설정 - 시스템 폰트 사용
        val font: PdfFont = getKoreanFont()

        // 1. 제목 섹션
        document.add(
            Paragraph("${user.userNm} (${user.userId})님의 건강 리포트")
                .setFont(font)
                .setBold()
                .setFontSize(18f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10f)
        )

        // 2. 출력 날짜 추가
        val outputDate = LocalDateTime.now().format(dateTimeFormatter)
        document.add(
            Paragraph("출력 날짜: $outputDate")
                .setFont(font)
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10f)
        )

        // 3. 기간 정보
        val periodText = when(period.lowercase()) {
            "daily" -> "일자별"
            "weekly" -> "주간별"
            "monthly" -> "월간별"
            else -> period
        }
        document.add(
            Paragraph("기간: $periodText")
                .setFont(font)
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20f)
        )

        // 4. 건강 요약 섹션
        val avgSleep = String.format("%.1f", healthLogs.map { it.sleepHours }.average())
        val avgSteps = String.format("%.0f", healthLogs.map { it.steps }.average())
        val avgStress = String.format("%.1f", healthLogs.map { it.stressLevel }.average())
        val avgHeartRate = String.format("%.0f", healthLogs.map { it.heartRate }.average())

        document.add(
            Paragraph("건강 요약")
                .setFont(font)
                .setBold()
                .setFontSize(14f)
                .setMarginBottom(5f)
        )
        document.add(
            Paragraph("평균 수면 시간: $avgSleep 시간, 평균 걸음 수: $avgSteps 보, 평균 스트레스: $avgStress, 평균 심박수: $avgHeartRate bpm")
                .setFont(font)
                .setFontSize(12f)
                .setMarginBottom(15f)
        )

        // 5. 차트 생성 및 삽입
        val chartImage = createHeartRateChart(healthLogs)
        val chartBaos = ByteArrayOutputStream()
        ImageIO.write(chartImage, "png", chartBaos)
        val chartBytes = chartBaos.toByteArray()
        val chartImageElement = Image(ImageDataFactory.create(chartBytes))
            .setWidth(UnitValue.createPercentValue(90f)) // 크기를 약간 늘림
            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(chartImageElement)

        // 6. 건강 데이터 테이블
        val columnWidths = floatArrayOf(12f, 12f, 12f, 12f, 12f, 12f, 12f, 12f)
        val unitValues = columnWidths.map { UnitValue.createPercentValue(it) }.toTypedArray()
        val table = Table(unitValues)
            .setWidth(UnitValue.createPercentValue(100f))
            .setMarginBottom(20f)

        // 테이블 헤더
        table.addHeaderCell(Paragraph("로그 ID").setFont(font).setBold())
        table.addHeaderCell(Paragraph("수면 시간").setFont(font).setBold())
        table.addHeaderCell(Paragraph("걸음 수").setFont(font).setBold())
        table.addHeaderCell(Paragraph("스트레스 수치").setFont(font).setBold())
        table.addHeaderCell(Paragraph("심박수").setFont(font).setBold())
        table.addHeaderCell(Paragraph("기록 날짜").setFont(font).setBold())
        table.addHeaderCell(Paragraph("경고").setFont(font).setBold())
        table.addHeaderCell(Paragraph("감정").setFont(font).setBold())

        // 테이블 데이터
        healthLogs.forEach { log ->
            table.addCell(Paragraph(log.logId.toString()).setFont(font))
            table.addCell(Paragraph(log.sleepHours.toString()).setFont(font))
            table.addCell(Paragraph(log.steps.toString()).setFont(font))
            table.addCell(Paragraph(log.stressLevel.toString()).setFont(font))
            table.addCell(Paragraph(log.heartRate.toString()).setFont(font))
            table.addCell(Paragraph(log.logDate.format(dateTimeFormatter)).setFont(font))
            table.addCell(Paragraph(if (log.warning) "있음" else "없음").setFont(font))
            table.addCell(Paragraph(log.memo?.let { sentimentAnalysisService.analyzeSentiment(it) } ?: "없음").setFont(font))
        }

        document.add(table)

        // 7. 문서 닫기
        document.close()
        return baos.toByteArray()
    }

    /**
     * 한글 폰트를 반환하는 함수
     * 여러 폰트를 시도해서 사용 가능한 폰트를 찾음
     */
    private fun getKoreanFont(): PdfFont {
        val fontPaths = listOf(
            // Windows
            "C:\\Windows\\Fonts\\malgun.ttf",
            "C:\\Windows\\Fonts\\gulim.ttc",
            "C:\\Windows\\Fonts\\batang.ttc",
            // macOS
            "/System/Library/Fonts/AppleSDGothicNeo.ttc",
            "/Library/Fonts/AppleMyungjo.ttf",
            // Linux
            "/usr/share/fonts/truetype/nanum/NanumGothic.ttf",
            "/usr/share/fonts/truetype/nanum/NanumMyeongjo.ttf",
            // 프로젝트 내부 폰트 (옵션)
            "src/main/resources/fonts/malgun.ttf",
            "src/main/resources/fonts/NanumGothic.ttf"
        )

        // 시스템에서 사용 가능한 폰트 찾기
        for (fontPath in fontPaths) {
            try {
                val file = java.io.File(fontPath)
                if (file.exists()) {
                    return PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H)
                }
            } catch (e: Exception) {
                // 이 폰트는 사용할 수 없음, 다음 폰트 시도
                continue
            }
        }

        // 내장 폰트로 폴백 (한글 지원 안 됨)
        println("Warning: 한글 폰트를 찾을 수 없어 기본 폰트를 사용합니다. 한글이 제대로 표시되지 않을 수 있습니다.")
        return try {
            PdfFontFactory.createFont("Helvetica", PdfEncodings.IDENTITY_H)
        } catch (e: Exception) {
            PdfFontFactory.createFont()
        }
    }

    private fun createHeartRateChart(healthLogs: List<HealthLog>): BufferedImage {
        val dataset = DefaultCategoryDataset()
        healthLogs.forEach { log ->
            dataset.addValue(log.heartRate.toDouble(), "심박수", log.logDate.format(DateTimeFormatter.ofPattern("MM/dd")))
        }

        val chart: JFreeChart = ChartFactory.createBarChart(
            "평균 심박수 추이",
            "날짜",
            "심박수 (bpm)",
            dataset
        )

        // 차트 폰트 설정 (한글 지원)
        val koreanFont = getSystemKoreanFont()

        // 제목 폰트 설정
        chart.title.font = koreanFont.deriveFont(Font.BOLD, 14f)

        // 축 레이블 폰트 설정
        val plot = chart.categoryPlot
        plot.domainAxis.labelFont = koreanFont.deriveFont(Font.PLAIN, 12f)
        plot.rangeAxis.labelFont = koreanFont.deriveFont(Font.PLAIN, 12f)

        // 축 눈금 레이블 폰트 설정 (더 작은 크기)
        plot.domainAxis.tickLabelFont = koreanFont.deriveFont(Font.PLAIN, 10f)
        plot.rangeAxis.tickLabelFont = koreanFont.deriveFont(Font.PLAIN, 10f)

        // 범례 폰트 설정
        if (chart.legend != null) {
            chart.legend.itemFont = koreanFont.deriveFont(Font.PLAIN, 10f)
        }

        plot.setBackgroundPaint(java.awt.Color.WHITE)
        plot.setRangeGridlinePaint(java.awt.Color.GRAY)

        // 차트 크기를 충분히 크게 해서 글자 잘림 방지
        return chart.createBufferedImage(500, 350)
    }

    /**
     * 차트용 시스템 한글 폰트를 반환하는 함수
     */
    private fun getSystemKoreanFont(): Font {
        val fontNames = listOf(
            "Malgun Gothic",
            "맑은 고딕",
            "Gulim",
            "굴림",
            "Batang",
            "바탕",
            "Apple SD Gothic Neo",
            "Nanum Gothic",
            "나눔고딕"
        )

        for (fontName in fontNames) {
            try {
                val font = Font(fontName, Font.PLAIN, 12)
                // 폰트가 실제로 존재하는지 확인
                if (font.family != Font.DIALOG) {
                    return font
                }
            } catch (e: Exception) {
                continue
            }
        }

        // 폴백으로 시스템 기본 폰트 사용
        return Font(Font.SANS_SERIF, Font.PLAIN, 12)
    }
}