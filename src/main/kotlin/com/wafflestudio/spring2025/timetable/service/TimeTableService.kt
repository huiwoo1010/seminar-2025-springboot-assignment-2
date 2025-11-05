package com.wafflestudio.spring2025.timetable.service

import com.wafflestudio.spring2025.course.CourseNotFoundException
import com.wafflestudio.spring2025.timetable.TimeTableForbiddenException
import com.wafflestudio.spring2025.timetable.TimeTableNameBlankException
import com.wafflestudio.spring2025.timetable.TimeTableNotFoundException
import com.wafflestudio.spring2025.course.dto.corre.CourseDto
import com.wafflestudio.spring2025.timetable.dto.core.TimeTableDetailDto
import com.wafflestudio.spring2025.timetable.dto.core.TimeTableDto
import com.wafflestudio.spring2025.timetable.model.Semester
import com.wafflestudio.spring2025.timetable.model.TimeTable
import com.wafflestudio.spring2025.timetable.repository.TimeTableCourseRepository
import com.wafflestudio.spring2025.timetable.repository.TimeTableRepository
import com.wafflestudio.spring2025.user.model.User
import com.wafflestudio.spring2025.course.repository.CourseRepository
import com.wafflestudio.spring2025.timetable.model.TimeTableCourse
import org.springframework.stereotype.Service
import com.wafflestudio.spring2025.course.repository.CourseTimeSlotRepository
import com.wafflestudio.spring2025.timetable.TimeTableCourseOverlappedException

@Service
class TimeTableService(
    private val timeTableRepository: TimeTableRepository,
    private val timeTableCourseRepository: TimeTableCourseRepository,
    private val courseRepository: CourseRepository,
    private val courseTimeSlotRepository: CourseTimeSlotRepository,
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

    fun addCourse(timeTableId: Long, courseId: Long, user: User): TimeTableDetailDto {
        val timetable = timeTableRepository.findById(timeTableId).orElseThrow { TimeTableNotFoundException() }
        val course = courseRepository.findById(courseId).orElseThrow { CourseNotFoundException() }
        if (timetable.userId != user.id) {
            throw TimeTableForbiddenException()
        }
        val existingLink = timeTableCourseRepository.findByTimetableIdAndCourseId(timeTableId, courseId)
        if (existingLink != null) {
            return detail(timeTableId, user)
        }

        val existingLinks = timeTableCourseRepository.findAllByTimetableId(timeTableId)
        val existingCourseIds = existingLinks.map { it.courseId }
        val existingSlots = if (existingCourseIds.isNotEmpty()) {
            courseTimeSlotRepository.findAllByCourseIdIn(existingCourseIds)
        } else emptyList()


        val newSlots = courseTimeSlotRepository.findAllByCourseId(course.id!!)

        // 겹침 검사: 요일이 같고, 구간이 겹치면 충돌
        fun overlap(s1: Int, e1: Int, s2: Int, e2: Int): Boolean = s1 < e2 && s2 < e1
        val hasConflict = newSlots.any { ns ->
            existingSlots.any { es -> es.day == ns.day && overlap(es.startMin, es.endMin, ns.startMin, ns.endMin) }
        }
        if (hasConflict) {
            throw TimeTableCourseOverlappedException()
        }

        timeTableCourseRepository.save(
            TimeTableCourse(
                timetableId = timetable.id!!,
                courseId = course.id!!,
            ),
        )
        return detail(timeTableId, user)
    }

    fun removeCourse(timeTableId: Long, courseId: Long, user: User): TimeTableDetailDto {
        val timetable = timeTableRepository.findById(timeTableId).orElseThrow { TimeTableNotFoundException() }
        if (timetable.userId != user.id) {
            throw TimeTableForbiddenException()
        }
        val link = timeTableCourseRepository.findByTimetableIdAndCourseId(timeTableId, courseId)
            ?: throw CourseNotFoundException()

        timeTableCourseRepository.delete(link)
        return detail(timeTableId, user)
    }
}