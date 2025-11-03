package com.wafflestudio.spring2025.timetable.dto.core

import com.wafflestudio.spring2025.timetable.model.Semester
import com.wafflestudio.spring2025.timetable.model.TimeTable

data class TimeTableDto(
    val id: Long,
    val name: String,
    val semester: Semester,
    val year: Int
) {
    constructor(timetable: TimeTable): this(
        id = timetable.id!!,
        name = timetable.name,
        semester = timetable.semester,
        year = timetable.year
    )
}
