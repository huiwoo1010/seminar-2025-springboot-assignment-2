package com.wafflestudio.spring2025.sugangsnu.service

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.model.CourseTimeSlot
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.course.repository.CourseTimeSlotRepository
import com.wafflestudio.spring2025.sugangsnu.repository.SugangSnuRepository
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.stereotype.Service
import java.time.DayOfWeek

data class ImportResult(
    val imported: Int,
    val updated: Int,
)

@Service
class SugangSnuFetchService(
    private val sugangRepo: SugangSnuRepository,
    private val courseRepository: CourseRepository,
    private val courseTimeSlotRepository: CourseTimeSlotRepository, // add repository injection
) {
    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun importFromSugang(
        year: Int,
        term: Term,
        @Suppress("UNUSED_PARAMETER") lang: String = "ko", // lang은 무시해도 되지만 인터페이스 유지
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

            fun parseSlots(raw: String): List<Triple<DayOfWeek, Int, Int>> {
                if (raw.isBlank()) return emptyList()
                val items = raw.split('/').map { it.trim() }.filter { it.isNotEmpty() }
                val list = mutableListOf<Triple<DayOfWeek, Int, Int>>()
                val reKo = Regex("^([월화수목금토일])\\((\\d{1,2}):(\\d{2})~(\\d{1,2}):(\\d{2})\\)$")
                val reEn = Regex("^(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\((\\d{1,2}):(\\d{2})~(\\d{1,2}):(\\d{2})\\)$", RegexOption.IGNORE_CASE)

                fun toMin(
                    h: Int,
                    m: Int,
                ) = h * 60 + m

                fun dayFromKo(ch: Char) =
                    when (ch) {
                        '월' -> DayOfWeek.MONDAY
                        '화' -> DayOfWeek.TUESDAY
                        '수' -> DayOfWeek.WEDNESDAY
                        '목' -> DayOfWeek.THURSDAY
                        '금' -> DayOfWeek.FRIDAY
                        '토' -> DayOfWeek.SATURDAY
                        '일' -> DayOfWeek.SUNDAY
                        else -> null
                    }

                fun dayFromEn(s: String) =
                    when (s.lowercase()) {
                        "mon" -> DayOfWeek.MONDAY
                        "tue" -> DayOfWeek.TUESDAY
                        "wed" -> DayOfWeek.WEDNESDAY
                        "thu" -> DayOfWeek.THURSDAY
                        "fri" -> DayOfWeek.FRIDAY
                        "sat" -> DayOfWeek.SATURDAY
                        "sun" -> DayOfWeek.SUNDAY
                        else -> null
                    }
                for (it in items) {
                    val m1 = reKo.matchEntire(it)
                    if (m1 != null) {
                        val day = dayFromKo(m1.groupValues[1][0]) ?: continue
                        val sh = m1.groupValues[2].toInt()
                        val sm = m1.groupValues[3].toInt()
                        val eh = m1.groupValues[4].toInt()
                        val em = m1.groupValues[5].toInt()
                        val start = toMin(sh, sm)
                        val end = toMin(eh, em)
                        if (end > start) list += Triple(day, start, end)
                        continue
                    }
                    val m2 = reEn.matchEntire(it)
                    if (m2 != null) {
                        val day = dayFromEn(m2.groupValues[1]) ?: continue
                        val sh = m2.groupValues[2].toInt()
                        val sm = m2.groupValues[3].toInt()
                        val eh = m2.groupValues[4].toInt()
                        val em = m2.groupValues[5].toInt()
                        val start = toMin(sh, sm)
                        val end = toMin(eh, em)
                        if (end > start) list += Triple(day, start, end)
                        continue
                    }
                    // 형식과 맞지 않으면 무시
                }
                return list
            }

            rows.forEach { row ->
                val courseNumber = row.get("교과목번호")
                val lectureNumber = row.get("강좌번호")
                if (courseNumber.isEmpty() || lectureNumber.isEmpty()) return@forEach

                val titleMain = row.get("교과목명")
                val titleSub = row.get("부제명")
                val fullTitle = if (titleSub.isEmpty()) titleMain else "$titleMain ($titleSub)"

                val timeRaw = row.get("수업교시")

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
                        rawTime = timeRaw,
                        courseCode = courseNumber,
                        classCode = lectureNumber,
                        title = fullTitle,
                        credit = row.get("학점").toIntOrNull() ?: 0,
                        professor = row.get("주담당교수"),
                        room = row.get("강의실(동-호)(#연건, *평창)"),
                    )

                val slots = parseSlots(timeRaw)

                val existing = courseRepository.findCourse(year, term, courseNumber, lectureNumber)
                val saved =
                    if (existing == null) {
                        val c = courseRepository.save(course)
                        imported++
                        c
                    } else {
                        val c = courseRepository.save(course.copy(id = existing.id))
                        updated++
                        c
                    }

                // 수업교시 저장 (기존 삭제 후 재생성)
                saved.id?.let { cid ->
                    courseTimeSlotRepository.deleteByCourseId(cid)
                    if (slots.isNotEmpty()) {
                        val entities =
                            slots.map { (day, s, e) ->
                                CourseTimeSlot(
                                    id = null,
                                    courseId = cid,
                                    day = day,
                                    startMin = s,
                                    endMin = e,
                                )
                            }
                        courseTimeSlotRepository.saveAll(entities)
                    }
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
