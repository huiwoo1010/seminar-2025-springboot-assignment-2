package com.wafflestudio.spring2025.timetable.service

import com.wafflestudio.spring2025.timetable.TimeTableForbiddenException
import com.wafflestudio.spring2025.timetable.TimeTableNameBlankException
import com.wafflestudio.spring2025.timetable.TimeTableNotFoundException
import com.wafflestudio.spring2025.timetable.dto.core.CourseDto
import com.wafflestudio.spring2025.timetable.dto.core.TimeTableDetailDto
import com.wafflestudio.spring2025.timetable.dto.core.TimeTableDto
import com.wafflestudio.spring2025.timetable.model.Semester
import com.wafflestudio.spring2025.timetable.model.TimeTable
import com.wafflestudio.spring2025.timetable.repository.TimeTableCourseRepository
import com.wafflestudio.spring2025.timetable.repository.TimeTableRepository
import com.wafflestudio.spring2025.user.model.User
import com.wafflestudio.spring2025.course.repository.CourseRepository
import org.springframework.stereotype.Service

@Service
class TimeTableService(
    private val timeTableRepository: TimeTableRepository,
    private val timeTableCourseRepository: TimeTableCourseRepository,
    private val courseRepository: CourseRepository,
) {
    fun create(name: String, year: Int, semester: Semester, user: User): TimeTableDto {
        if (name.isBlank()) {
            throw TimeTableNameBlankException()
        }

        val timetable =
            timeTableRepository.save(
                TimeTable(
                    name = name,
                    year = year,
                    semester = semester,
                    userId = user.id!!,
                ),
            )
        return TimeTableDto(timetable)
    }

    fun list(user: User): List<TimeTableDto> {
        val timetables = timeTableRepository.findAllByUserId(user.id!!)
        return timetables.map { TimeTableDto(it) }
    }

    fun detail(id: Long, user: User): TimeTableDetailDto {
        val timetable = timeTableRepository.findById(id).orElseThrow { TimeTableNotFoundException() }
        if (timetable.userId != user.id) {
            throw TimeTableForbiddenException()
        }

        val links = timeTableCourseRepository.findAllByTimetableId(timetable.id!!)
        val courseIds = links.map { it.courseId }
        val courses = if (courseIds.isNotEmpty()) courseRepository.findAllById(courseIds) else emptyList()
        val courseDtos = courses.map { CourseDto(it) }
        val totalCredits = courses.sumOf { it.credit }
        return TimeTableDetailDto(timetable, totalCredits, courseDtos)
    }

    fun update(id: Long, name: String, user: User): TimeTableDto {
        val timetable = timeTableRepository.findById(id).orElseThrow { TimeTableNotFoundException() }
        if (timetable.userId != user.id) {
            throw TimeTableForbiddenException()
        }
        if (name.isBlank()) {
            throw TimeTableNameBlankException()
        }

        val updatedTimeTable = timetable.copy(name = name)
        timeTableRepository.save(updatedTimeTable)
        return TimeTableDto(updatedTimeTable)
    }

    fun delete(id: Long, user: User) {
        val timetable = timeTableRepository.findById(id).orElseThrow { TimeTableNotFoundException() }
        if (timetable.userId != user.id) {
            throw TimeTableForbiddenException()
        }

        timeTableRepository.deleteById(id)
    }
}