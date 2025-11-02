package com.wafflestudio.spring2025.sugangsnu.service

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.model.CourseTimeSlot
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.course.repository.CourseTimeSlotRepository
import com.wafflestudio.spring2025.sugangsnu.controller.ImportResult
import com.wafflestudio.spring2025.sugangsnu.repository.SugangSnuRepository
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.io.ByteArrayInputStream
import java.time.DayOfWeek

@Service
class SugangSnuFetchService(
    @Qualifier("SugangSnuWebClient") private val webClient: WebClient,
    private val sugangRepo: SugangSnuRepository,
    private val courseRepo: CourseRepository,
    private val slotRepo: CourseTimeSlotRepository,
) {
    suspend fun importFromSugang(
        year: Int,
        term: Term,
        lang: String = "ko",
    ): ImportResult {
        val buffer = sugangRepo.getSugangSnuLectures(year, term, lang)
        return try {
            val bytes = buffer.asInputStream().readBytes()
            val workbook = workbookFor(bytes)
            parseAndUpsert(workbook, year, term)
        } finally {
            // 반드시 버퍼 해제 (메모리 누수 방지)
            try {
                buffer.release()
            } catch (_: Throwable) {
            }
        }
    }

    private fun workbookFor(bytes: ByteArray): Workbook {
        val isZipHeader =
            bytes.size >= 4 &&
                bytes[0] == 0x50.toByte() &&
                bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() &&
                bytes[3] == 0x04.toByte()
        return if (isZipHeader) {
            XSSFWorkbook(ByteArrayInputStream(bytes))
        } else {
            HSSFWorkbook(ByteArrayInputStream(bytes))
        }
    }

    private fun parseAndUpsert(
        wb: Workbook,
        year: Int,
        term: Term,
    ): ImportResult {
        val sheet = wb.getSheetAt(0)
        val headerRowIdx = 2 // SNUTT 기준 3번째 줄(인덱스 2)
        val header = sheet.getRow(headerRowIdx)
        val colIndex: Map<String, Int> =
            (0 until header.lastCellNum).associateBy(
                keySelector = { i ->
                    header
                        .getCell(i)
                        ?.stringCellValue
                        ?.trim()
                        .orEmpty()
                },
                valueTransform = { i -> i },
            )

        fun Row.getText(col: String): String = this.getCell(colIndex[col] ?: -1)?.let { cellToString(it) } ?: ""

        var imported = 0
        var updated = 0

        for (r in (headerRowIdx + 1)..sheet.lastRowNum) {
            val row = sheet.getRow(r) ?: continue
            val courseCode = row.getText("교과목번호")
            val classCode = row.getText("강좌번호")
            if (courseCode.isBlank() || classCode.isBlank()) continue

            val title = buildTitle(row.getText("교과목명"), row.getText("부제명"))
            val professor = row.getText("주담당교수").substringBeforeLast(" (")
            val credit = row.getText("학점").toIntOrNull() ?: 0
            val category = row.getText("교과구분").ifBlank { null }
            val college = row.getText("개설대학").ifBlank { null }
            val department = row.getText("개설학과").ifBlank { college }
            val program = row.getText("이수과정").ifBlank { null }
            val grade = row.getText("학년").toIntOrNull()
            val timeText = row.getText("수업교시")
            val roomText = row.getText("강의실(동-호)(#연건, *평창)")

            val existing = courseRepo.findOne(year, term, courseCode, classCode)
            val saved =
                if (existing == null) {
                    imported++
                    courseRepo.save(
                        Course(
                            year = year,
                            term = term,
                            category = category,
                            college = college,
                            department = department,
                            program = program,
                            grade = grade,
                            courseCode = courseCode,
                            classCode = classCode,
                            title = title,
                            credit = credit,
                            professor = professor,
                            room = roomText,
                        ),
                    )
                } else {
                    updated++
                    existing
                        .apply {
                            this.category = category
                            this.college = college
                            this.department = department
                            this.program = program
                            this.grade = grade
                            this.title = title
                            this.credit = credit
                            this.professor = professor
                            this.room = roomText
                        }.let(courseRepo::save)
                }

            slotRepo.deleteByCourseId(saved.id!!)
            parseTimeSlots(timeText, roomText).forEach { s ->
                slotRepo.save(
                    CourseTimeSlot(
                        courseId = saved.id!!,
                        day = s.day,
                        startMin = s.startMin,
                        endMin = s.endMin,
                        place = s.place,
                    ),
                )
            }
        }

        wb.close()
        return ImportResult(imported, updated)
    }

    private fun cellToString(cell: Cell): String =
        when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                val n = cell.numericCellValue
                if (n % 1.0 == 0.0) n.toLong().toString() else n.toString()
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> cell.cachedFormulaResultType?.let { cellToString(cell) } ?: cell.toString()
            else -> cell.toString().trim()
        }

    private fun buildTitle(
        title: String,
        subtitle: String,
    ): String = if (subtitle.isBlank()) title else "$title ($subtitle)"

    private data class Slot(
        val day: DayOfWeek,
        val startMin: Int,
        val endMin: Int,
        val place: String?,
    )

    private fun parseTimeSlots(
        timeText: String,
        roomText: String?,
    ): List<Slot> {
        if (timeText.isBlank()) return emptyList()
        val parts = timeText.split("/", " / ", " , ").map { it.trim() }.filter { it.isNotBlank() }
        val rooms = (roomText ?: "").split("/", " / ").map { it.trim() }.filter { it.isNotBlank() }
        val slots = mutableListOf<Slot>()
        parts.forEachIndexed { idx, token ->
            val day =
                when (token.firstOrNull()) {
                    '월' -> DayOfWeek.MONDAY
                    '화' -> DayOfWeek.TUESDAY
                    '수' -> DayOfWeek.WEDNESDAY
                    '목' -> DayOfWeek.THURSDAY
                    '금' -> DayOfWeek.FRIDAY
                    '토' -> DayOfWeek.SATURDAY
                    '일' -> DayOfWeek.SUNDAY
                    else -> return@forEachIndexed
                }
            val range = token.substringAfter("(").substringBefore(")")
            if (range.contains("-")) {
                val (s, e) = range.split("-").map { it.trim() }
                val startMin = hmToMin(s)
                val endMin = hmToMin(e)
                val place = rooms.getOrNull(idx)
                if (startMin in 0 until endMin) slots += Slot(day, startMin, endMin, place)
            }
        }
        return slots
    }

    private fun hmToMin(hm: String): Int {
        val (h, m) = hm.split(":").map { it.trim().toInt() }
        return h * 60 + m
    }
}
