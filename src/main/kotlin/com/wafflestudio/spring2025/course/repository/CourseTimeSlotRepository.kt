package com.wafflestudio.spring2025.course.repository

import com.wafflestudio.spring2025.course.model.CourseTimeSlot
import org.springframework.data.repository.ListCrudRepository

interface CourseTimeSlotRepository : ListCrudRepository<CourseTimeSlot, Long> {
    fun deleteByCourseId(courseId: Long)
    fun findAllByCourseId(courseId: Long): List<CourseTimeSlot>
    fun findAllByCourseIdIn(courseIds: Collection<Long>): List<CourseTimeSlot>
}
