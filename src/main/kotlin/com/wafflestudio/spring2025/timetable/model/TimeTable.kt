package com.wafflestudio.spring2025.timetable.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("timetables")
data class TimeTable(
    @Id var id: Long? = null,
    var name: String,
    var semester: Semester,
    var year: Int,
    @Column("user_id")
    var userId: Long,
)
