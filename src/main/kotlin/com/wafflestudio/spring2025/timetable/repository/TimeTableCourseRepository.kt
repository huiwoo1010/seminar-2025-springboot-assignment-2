package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.timetable.model.TimeTableCourse
import org.springframework.data.repository.ListCrudRepository

interface TimeTableCourseRepository : ListCrudRepository<TimeTableCourse, Long> {
    fun findAllByTimetableId(timetableId: Long): List<TimeTableCourse>
}

