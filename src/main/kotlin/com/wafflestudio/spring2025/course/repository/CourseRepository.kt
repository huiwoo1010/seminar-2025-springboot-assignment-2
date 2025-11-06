package com.wafflestudio.spring2025.course.repository

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.course.model.Course
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository

interface CourseRepository : ListCrudRepository<Course, Long> {
    @Query(
        """
        SELECT * FROM courses
        WHERE year = :year
          AND term = :term
          AND course_code = :courseCode
          AND class_code = :classCode
        LIMIT 1
    """,
    )
    fun findCourse(
        year: Int,
        term: Term,
        courseCode: String,
        classCode: String,
    ): Course?

    @Query(
        """
        SELECT * FROM courses
        WHERE year = :year
          AND term = :term
          AND (:query IS NULL OR :query = ''
               OR title LIKE CONCAT('%', :query, '%')
               OR professor LIKE CONCAT('%', :query, '%'))
        ORDER BY id
        LIMIT :limit OFFSET :offset
    """,
    )
    fun searchWithPagination(
        year: Int,
        term: Term,
        query: String?,
        limit: Int,
        offset: Int,
    ): List<Course>

    @Query(
        """
        SELECT COUNT(*) FROM courses
        WHERE year = :year
          AND term = :term
          AND (:query IS NULL OR :query = ''
               OR title LIKE CONCAT('%', :query, '%')
               OR professor LIKE CONCAT('%', :query, '%'))
    """,
    )
    fun countByYearAndTermAndQuery(
        year: Int,
        term: Term,
        query: String?,
    ): Long
}
