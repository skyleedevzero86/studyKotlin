package com.kochat.global.application.admin

import com.kochat.adapter.inbound.web.admin.dto.MessageTypeYearStatisticsResponse
import com.kochat.adapter.inbound.web.admin.dto.RoomTypeDailyStatisticsResponse
import com.kochat.adapter.inbound.web.admin.dto.StatisticsPeriodResponse
import com.kochat.adapter.inbound.web.admin.dto.UserEventDailyStatisticsResponse
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
class AdminStatisticsExportService {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

    fun exportHourlyExcel(data: StatisticsPeriodResponse): ByteArray =
        buildWorkbook(data.title) { sheet, styles ->
            var rowIdx = writeMetaRows(sheet, styles, data.title, data.from, data.to, mapOf(
                "채팅방 유형" to (data.roomType ?: "전체"),
                "메시지 유형" to (data.messageType ?: "전체"),
            ))
            rowIdx = writeHeaderRow(sheet, styles, rowIdx, listOf("시간대", "메시지 건수", "비율(%)"))
            data.rows.forEach { row ->
                writeDataRow(sheet, styles, rowIdx++, listOf(row.label, row.count.toString(), "${row.ratio}%"))
            }
            writeTotalRow(sheet, styles, rowIdx, listOf("계", data.total.toString(), "100%"))
        }

    fun exportHourlyPdf(data: StatisticsPeriodResponse): ByteArray =
        buildPdf(data.title, data.from, data.to) {
            addTable(
                headers = listOf("시간대", "메시지 건수", "비율(%)"),
                rows = data.rows.map { listOf(it.label, it.count.toString(), "${it.ratio}%") },
                totalRow = listOf("계", data.total.toString(), "100%"),
            )
        }

    fun exportMessageTypeYearExcel(data: MessageTypeYearStatisticsResponse): ByteArray =
        buildWorkbook(data.title) { sheet, styles ->
            var rowIdx = writeMetaRows(sheet, styles, data.title, data.from, data.to, mapOf(
                "채팅방 유형" to (data.roomType ?: "전체"),
            ))
            val headers = mutableListOf("구분")
            data.typeLabels.forEach { type ->
                headers.add("${messageTypeLabel(type)} 건수")
                headers.add("${messageTypeLabel(type)} 비율")
            }
            headers.add("합계")
            rowIdx = writeHeaderRow(sheet, styles, rowIdx, headers)
            data.rows.forEach { row ->
                val cells = mutableListOf<String>("${row.year}년")
                data.typeLabels.forEach { type ->
                    val item = row.types[type]
                    cells.add((item?.count ?: 0L).toString())
                    cells.add("${item?.ratio ?: 0.0}%")
                }
                cells.add(row.total.toString())
                writeDataRow(sheet, styles, rowIdx++, cells)
            }
            val totalCells = mutableListOf("계")
            data.typeLabels.forEach { type ->
                val item = data.totals[type]
                totalCells.add((item?.count ?: 0L).toString())
                totalCells.add("${item?.ratio ?: 0.0}%")
            }
            totalCells.add(data.grandTotal.toString())
            writeTotalRow(sheet, styles, rowIdx, totalCells)
        }

    fun exportMessageTypeYearPdf(data: MessageTypeYearStatisticsResponse): ByteArray =
        buildPdf(data.title, data.from, data.to) {
            val headers = mutableListOf("구분")
            data.typeLabels.forEach { type ->
                headers.add("${messageTypeLabel(type)} 건수")
                headers.add("${messageTypeLabel(type)} 비율")
            }
            headers.add("합계")
            val rows = data.rows.map { row ->
                val cells = mutableListOf("${row.year}년")
                data.typeLabels.forEach { type ->
                    val item = row.types[type]
                    cells.add((item?.count ?: 0L).toString())
                    cells.add("${item?.ratio ?: 0.0}%")
                }
                cells.add(row.total.toString())
                cells.toList()
            }
            val totalCells = mutableListOf("계")
            data.typeLabels.forEach { type ->
                val item = data.totals[type]
                totalCells.add((item?.count ?: 0L).toString())
                totalCells.add("${item?.ratio ?: 0.0}%")
            }
            totalCells.add(data.grandTotal.toString())
            addTable(headers, rows, totalCells)
        }

    fun exportRoomTypeDailyExcel(data: RoomTypeDailyStatisticsResponse): ByteArray =
        buildWorkbook(data.title) { sheet, styles ->
            var rowIdx = writeMetaRows(sheet, styles, data.title, data.from, data.to, mapOf(
                "메시지 유형" to (data.messageType ?: "전체"),
            ))
            val headers = mutableListOf("일자")
            data.typeLabels.forEach { type ->
                headers.add("${roomTypeLabel(type)} 건수")
                headers.add("${roomTypeLabel(type)} 비율")
            }
            headers.add("합계")
            rowIdx = writeHeaderRow(sheet, styles, rowIdx, headers)
            data.rows.forEach { row ->
                val cells = mutableListOf(row.date.toString())
                data.typeLabels.forEach { type ->
                    val item = row.types[type]
                    cells.add((item?.count ?: 0L).toString())
                    cells.add("${item?.ratio ?: 0.0}%")
                }
                cells.add(row.total.toString())
                writeDataRow(sheet, styles, rowIdx++, cells)
            }
            val totalCells = mutableListOf("계")
            data.typeLabels.forEach { type ->
                val item = data.totals[type]
                totalCells.add((item?.count ?: 0L).toString())
                totalCells.add("${item?.ratio ?: 0.0}%")
            }
            totalCells.add(data.grandTotal.toString())
            writeTotalRow(sheet, styles, rowIdx, totalCells)
        }

    fun exportRoomTypeDailyPdf(data: RoomTypeDailyStatisticsResponse): ByteArray =
        buildPdf(data.title, data.from, data.to) {
            val headers = mutableListOf("일자")
            data.typeLabels.forEach { type ->
                headers.add("${roomTypeLabel(type)} 건수")
                headers.add("${roomTypeLabel(type)} 비율")
            }
            headers.add("합계")
            val rows = data.rows.map { row ->
                val cells = mutableListOf(row.date.toString())
                data.typeLabels.forEach { type ->
                    val item = row.types[type]
                    cells.add((item?.count ?: 0L).toString())
                    cells.add("${item?.ratio ?: 0.0}%")
                }
                cells.add(row.total.toString())
                cells.toList()
            }
            val totalCells = mutableListOf("계")
            data.typeLabels.forEach { type ->
                val item = data.totals[type]
                totalCells.add((item?.count ?: 0L).toString())
                totalCells.add("${item?.ratio ?: 0.0}%")
            }
            totalCells.add(data.grandTotal.toString())
            addTable(headers, rows, totalCells)
        }

    fun exportUserEventDailyExcel(data: UserEventDailyStatisticsResponse): ByteArray =
        buildWorkbook(data.title) { sheet, styles ->
            var rowIdx = writeMetaRows(sheet, styles, data.title, data.from, data.to, mapOf(
                "활동 유형" to (data.eventType?.let(::userEventLabel) ?: "전체"),
            ))
            val headers = mutableListOf("일자")
            data.typeLabels.forEach { type ->
                headers.add("${userEventLabel(type)} 건수")
                headers.add("${userEventLabel(type)} 비율")
            }
            headers.add("합계")
            rowIdx = writeHeaderRow(sheet, styles, rowIdx, headers)
            data.rows.forEach { row ->
                val cells = mutableListOf(row.date.toString())
                data.typeLabels.forEach { type ->
                    val item = row.types[type]
                    cells.add((item?.count ?: 0L).toString())
                    cells.add("${item?.ratio ?: 0.0}%")
                }
                cells.add(row.total.toString())
                writeDataRow(sheet, styles, rowIdx++, cells)
            }
            val totalCells = mutableListOf("계")
            data.typeLabels.forEach { type ->
                val item = data.totals[type]
                totalCells.add((item?.count ?: 0L).toString())
                totalCells.add("${item?.ratio ?: 0.0}%")
            }
            totalCells.add(data.grandTotal.toString())
            writeTotalRow(sheet, styles, rowIdx, totalCells)
        }

    fun exportUserEventDailyPdf(data: UserEventDailyStatisticsResponse): ByteArray =
        buildPdf(data.title, data.from, data.to) {
            val headers = mutableListOf("일자")
            data.typeLabels.forEach { type ->
                headers.add("${userEventLabel(type)} 건수")
                headers.add("${userEventLabel(type)} 비율")
            }
            headers.add("합계")
            val rows = data.rows.map { row ->
                val cells = mutableListOf(row.date.toString())
                data.typeLabels.forEach { type ->
                    val item = row.types[type]
                    cells.add((item?.count ?: 0L).toString())
                    cells.add("${item?.ratio ?: 0.0}%")
                }
                cells.add(row.total.toString())
                cells.toList()
            }
            val totalCells = mutableListOf("계")
            data.typeLabels.forEach { type ->
                val item = data.totals[type]
                totalCells.add((item?.count ?: 0L).toString())
                totalCells.add("${item?.ratio ?: 0.0}%")
            }
            totalCells.add(data.grandTotal.toString())
            addTable(headers, rows, totalCells)
        }

    private fun buildWorkbook(
        sheetName: String,
        writer: (org.apache.poi.ss.usermodel.Sheet, ExcelStyles) -> Unit,
    ): ByteArray {
        XSSFWorkbook().use { workbook ->
            val sheet = workbook.createSheet(sheetName.take(31))
            val styles = ExcelStyles(workbook)
            writer(sheet, styles)
            sheet.setColumnWidth(0, 18 * 256)
            ByteArrayOutputStream().use { out ->
                workbook.write(out)
                return out.toByteArray()
            }
        }
    }

    private fun writeMetaRows(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        styles: ExcelStyles,
        title: String,
        from: java.time.LocalDate,
        to: java.time.LocalDate,
        filters: Map<String, String>,
    ): Int {
        var rowIdx = 0
        val titleRow = sheet.createRow(rowIdx++)
        titleRow.createCell(0).apply {
            setCellValue(title)
            cellStyle = styles.title
        }
        val periodRow = sheet.createRow(rowIdx++)
        periodRow.createCell(0).setCellValue("검색기간: ${from} ~ ${to}")
        filters.forEach { (key, value) ->
            val row = sheet.createRow(rowIdx++)
            row.createCell(0).setCellValue("$key: $value")
        }
        val printedRow = sheet.createRow(rowIdx++)
        printedRow.createCell(0).setCellValue("출력일자: ${dateTimeFormatter.format(LocalDateTime.now())}")
        rowIdx++
        return rowIdx
    }

    private fun writeHeaderRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        styles: ExcelStyles,
        rowIdx: Int,
        headers: List<String>,
    ): Int {
        val row = sheet.createRow(rowIdx)
        headers.forEachIndexed { index, header ->
            row.createCell(index).apply {
                setCellValue(header)
                cellStyle = styles.header
            }
        }
        return rowIdx + 1
    }

    private fun writeDataRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        styles: ExcelStyles,
        rowIdx: Int,
        values: List<String>,
    ) {
        val row = sheet.createRow(rowIdx)
        values.forEachIndexed { index, value ->
            row.createCell(index).apply {
                setCellValue(value)
                cellStyle = styles.data
            }
        }
    }

    private fun writeTotalRow(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        styles: ExcelStyles,
        rowIdx: Int,
        values: List<String>,
    ) {
        val row = sheet.createRow(rowIdx)
        values.forEachIndexed { index, value ->
            row.createCell(index).apply {
                setCellValue(value)
                cellStyle = styles.total
            }
        }
    }

    private fun buildPdf(
        title: String,
        from: java.time.LocalDate,
        to: java.time.LocalDate,
        block: PdfBuilder.() -> Unit,
    ): ByteArray {
        val output = ByteArrayOutputStream()
        val document = Document(PageSize.A4.rotate(), 36f, 36f, 36f, 36f)
        PdfWriter.getInstance(document, output)
        document.open()
        val font = resolvePdfFont(11f)
        val titleFont = resolvePdfFont(14f, style = Font.BOLD)
        document.add(Phrase(title, titleFont))
        document.add(Phrase("검색기간: $from ~ $to", font))
        document.add(Phrase("출력일자: ${dateTimeFormatter.format(LocalDateTime.now())}", font))
        document.add(Phrase(" ", font))
        PdfBuilder(document, font).block()
        document.close()
        return output.toByteArray()
    }

    private fun resolvePdfFont(size: Float, style: Int = Font.NORMAL): Font =
        runCatching {
            val baseFont = BaseFont.createFont("HYGoThic-Medium", "UniKS-UCS2-H", BaseFont.NOT_EMBEDDED)
            Font(baseFont, size, style)
        }.getOrElse {
            FontFactory.getFont(FontFactory.HELVETICA, size, style)
        }

    private fun messageTypeLabel(type: String): String = when (type) {
        "TEXT" -> "텍스트"
        "IMAGE" -> "이미지"
        "FILE" -> "파일"
        "LINK" -> "링크"
        "SYSTEM" -> "시스템"
        else -> type
    }

    private fun roomTypeLabel(type: String): String = when (type) {
        "DIRECT" -> "1:1"
        "GROUP" -> "그룹"
        "CHANNEL" -> "채널"
        else -> type
    }

    private fun userEventLabel(type: String): String = when (type) {
        "JOIN" -> "가입"
        "PASSWORD_CHANGE" -> "비밀번호변경"
        "SUSPEND" -> "정지"
        "WITHDRAW" -> "탈퇴"
        else -> type
    }

    private class ExcelStyles(workbook: XSSFWorkbook) {
        val title = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 14
            }
            setFont(font)
        }
        val header = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
        val data = workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
        val total = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
            fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private class PdfBuilder(
        private val document: Document,
        private val font: Font,
    ) {
        fun addTable(
            headers: List<String>,
            rows: List<List<String>>,
            totalRow: List<String>,
        ) {
            val table = PdfPTable(headers.size)
            table.widthPercentage = 100f
            headers.forEach { header ->
                table.addCell(headerCell(header))
            }
            rows.forEach { row ->
                row.forEach { value ->
                    table.addCell(dataCell(value))
                }
            }
            totalRow.forEach { value ->
                table.addCell(totalCell(value))
            }
            document.add(table)
        }

        private fun headerCell(text: String): PdfPCell =
            PdfPCell(Phrase(text, font)).apply {
                horizontalAlignment = Element.ALIGN_CENTER
                grayFill = 0.9f
            }

        private fun dataCell(text: String): PdfPCell =
            PdfPCell(Phrase(text, font))

        private fun totalCell(text: String): PdfPCell =
            PdfPCell(Phrase(text, font)).apply {
                grayFill = 0.85f
            }
    }
}
