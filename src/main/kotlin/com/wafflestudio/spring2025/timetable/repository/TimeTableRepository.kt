package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.timetable.model.TimeTable
import org.springframework.data.repository.CrudRepository

interface TimeTableRepository : CrudRepository<TimeTable, Long> {
    fun findAllByUserId(userId: Long): List<TimeTable>
}
