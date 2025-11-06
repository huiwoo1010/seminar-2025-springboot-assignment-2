package com.wafflestudio.spring2025.course.service

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.course.dto.CoursePagingResponse
import com.wafflestudio.spring2025.course.dto.corre.CourseDto
import com.wafflestudio.spring2025.course.repository.CourseRepository
import org.springframework.stereotype.Service
import kotlin.math.ceil

@Service
class CourseService(
    private val courseRepository: CourseRepository,
) {
    fun search(
        year: Int,
        semester: String,
        query: String?,
        page: Int,
        size: Int,
    ): CoursePagingResponse {
        val term = Term.valueOf(semester.uppercase())
        val offset = page * size

        // Database-level filtering and pagination
        val courses = courseRepository.searchWithPagination(year, term, query, size, offset)
        val totalElements = courseRepository.countByYearAndTermAndQuery(year, term, query)
        val totalPages = ceil(totalElements.toDouble() / size).toInt()

        val courseDtos = courses.map { CourseDto(it) }

        return CoursePagingResponse(
            content = courseDtos,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }
}
