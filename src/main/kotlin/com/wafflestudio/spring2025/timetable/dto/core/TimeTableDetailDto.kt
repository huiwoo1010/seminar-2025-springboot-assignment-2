package com.wafflestudio.spring2025.timetable.dto.core

import com.wafflestudio.spring2025.course.dto.corre.CourseDto
import com.wafflestudio.spring2025.timetable.model.TimeTable

data class TimeTableDetailDto(
    val timetable: TimeTableDto,
    val totalCredits: Int,
    val courses: List<CourseDto>,
) {
    constructor(timetable: TimeTable, totalCredits: Int, courses: List<CourseDto>) : this(
        timetable = TimeTableDto(timetable),
        totalCredits = totalCredits,
        courses = courses,
    )
}
