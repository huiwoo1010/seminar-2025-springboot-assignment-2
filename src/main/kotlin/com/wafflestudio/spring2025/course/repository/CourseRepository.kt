package com.wafflestudio.spring2025.course.repository

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.course.model.Course
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository

interface CourseRepository : ListCrudRepository<Course, Long> {
    @Query(
        """
        SELECT * FROM courses
        WHERE year = :year AND term = :term
          AND course_code = :courseCode AND class_code = :classCode
        LIMIT 1
    """,
    )
    fun findOne(
        year: Int,
        term: Term,
        courseCode: String,
        classCode: String,
    ): Course?
}
