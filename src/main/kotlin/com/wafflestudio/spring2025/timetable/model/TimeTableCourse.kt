package com.wafflestudio.spring2025.timetable.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("timetable_courses")
data class TimeTableCourse(
    @Id var id: Long? = null,
    @Column("timetable_id")
    var timetableId: Long,
    @Column("course_id")
    var courseId: Long,
)