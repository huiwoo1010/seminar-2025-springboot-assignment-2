package com.wafflestudio.spring2025.timetable.dto.core

import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.timetable.model.TimeTable

data class TimeTableDetailDto(
    val id: Long,
    val name: String,
    val semester: String,
    val year: Int,
    val totalCredits: Int,
    val courses: List<CourseDto>
) {
    constructor(timetable: TimeTable, totalCredits: Int, courses: List<CourseDto>) : this(
        id = timetable.id!!,
        name = timetable.name,
        semester = timetable.semester.name,
        year = timetable.year,
        totalCredits = totalCredits,
        courses = courses
    )
}