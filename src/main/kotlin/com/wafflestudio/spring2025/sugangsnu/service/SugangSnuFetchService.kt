package com.wafflestudio.spring2025.sugangsnu.service

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.sugangsnu.repository.SugangSnuRepository
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.stereotype.Service

data class ImportResult(
    val imported: Int,
    val updated: Int,
)

@Service
class SugangSnuFetchService(
    private val sugangRepo: SugangSnuRepository,
    private val courseRepository: CourseRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun importFromSugang(
        year: Int,
        term: Term,
        lang: String = "ko", // lang은 무시해도 되지만 인터페이스 유지
        cookie: String? = null,
        ua: String? = null,
        referer: String? = null,
    ): ImportResult {
        val ko = sugangRepo.downloadLecturesXls(year, term, "ko", cookie, ua, referer)
        val en = sugangRepo.downloadLecturesXls(year, term, "en", cookie, ua, referer)
        return parseTwoWorkbooks(ko, en, year, term)
    }

    private fun parseTwoWorkbooks(
        ko: PooledDataBuffer,
        en: PooledDataBuffer,
        year: Int,
        term: Term,
    ): ImportResult {
        try {
            val wbKo = workbookFor(ko.asInputStream().readBytes())
            val wbEn = workbookFor(en.asInputStream().readBytes())
            val shKo = wbKo.getSheetAt(0)
            val shEn = wbEn.getSheetAt(0)

            val merged: List<List<Cell>> = shKo.zip(shEn).map { (kr: Row, eng: Row) -> kr + eng }
            if (merged.size < 4) return ImportResult(0, 0)

            val header = merged[2].associate { it.stringCellValue.trim() to it.columnIndex }
            val rows = merged.drop(3)

            var imported = 0
            var updated = 0

            fun List<Cell>.get(name: String): String {
                val idx = header[name] ?: return ""
                val v = this[idx]
                return v.stringCellValue?.trim() ?: ""
            }

            rows.forEach { row ->
                val courseNumber = row.get("교과목번호")
                val lectureNumber = row.get("강좌번호")
                if (courseNumber.isEmpty() || lectureNumber.isEmpty()) return@forEach

                val titleMain = row.get("교과목명")
                val titleSub = row.get("부제명")
                val fullTitle = if (titleSub.isEmpty()) titleMain else "$titleMain ($titleSub)"

                val course =
                    Course(
                        id = null,
                        year = year,
                        term = term.name,
                        category = row.get("교과구분"),
                        college = row.get("개설대학"),
                        department = row.get("개설학과"),
                        program = row.get("이수과정"),
                        grade = row.get("학년").toIntOrNull(),
                        courseCode = courseNumber,
                        classCode = lectureNumber,
                        title = fullTitle,
                        credit = row.get("학점").toIntOrNull() ?: 0,
                        professor = row.get("주담당교수"),
                        room = row.get("강의실(동-호)(#연건, *평창)"),
                    )

                val existing = courseRepository.findCourse(year, term, courseNumber, lectureNumber)
                if (existing == null) {
                    courseRepository.save(course)
                    imported++
                } else {
                    courseRepository.save(course.copy(id = existing.id))
                    updated++
                }
            }

            log.info("Sugang import: $year ${term.name} imported=$imported, updated=$updated")
            return ImportResult(imported, updated)
        } finally {
            try {
                ko.release()
            } catch (_: Throwable) {
            }
            try {
                en.release()
            } catch (_: Throwable) {
            }
        }
    }

    private fun workbookFor(bytes: ByteArray): Workbook {
        // HTML 가드 (로그인/차단 페이지 등)
        if (bytes.isNotEmpty() && bytes[0].toInt() == '<'.code) {
            val preview = runCatching { String(bytes, Charsets.UTF_8).take(300) }.getOrNull()
            throw IllegalStateException("Got HTML instead of Excel. preview=$preview")
        }
        val isXlsx = bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() // "PK"
        return if (isXlsx) XSSFWorkbook(bytes.inputStream()) else HSSFWorkbook(bytes.inputStream())
    }
}
