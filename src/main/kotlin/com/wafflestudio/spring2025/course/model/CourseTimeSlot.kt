package com.wafflestudio.spring2025.course.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.DayOfWeek

@Table("course_time_slots")
class CourseTimeSlot(
    @Id
    var id: Long? = null,
    @Column("course_id")
    var courseId: Long,
    var day: DayOfWeek,
    @Column("start_min")
    var startMin: Int,
    @Column("end_min")
    var endMin: Int,
)
