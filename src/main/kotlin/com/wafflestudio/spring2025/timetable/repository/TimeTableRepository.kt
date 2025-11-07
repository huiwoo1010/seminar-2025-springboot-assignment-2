package com.wafflestudio.spring2025.timetable.repository

import com.wafflestudio.spring2025.timetable.model.TimeTable
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface TimeTableRepository : CrudRepository<TimeTable, Long> {
    fun findAllByUserId(userId: Long): List<TimeTable>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM timetables
            WHERE user_id = :userId
              AND name = :name
              AND year = :year
              AND semester = :semester
        )
    """,
    )
    fun existsByUserIdAndNameAndYearAndSemester(
        userId: Long,
        name: String,
        year: Int,
        semester: String,
    ): Boolean
}
