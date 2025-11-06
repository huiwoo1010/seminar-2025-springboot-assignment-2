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

        val allCourses = courseRepository.findByYearAndTerm(year, term)

        val filteredCourses =
            if (query.isNullOrBlank()) {
                allCourses
            } else {
                allCourses.filter { course ->
                    course.title.contains(query, ignoreCase = true) ||
                        (course.professor?.contains(query, ignoreCase = true) ?: false)
                }
            }

        val totalElements = filteredCourses.size.toLong()
        val totalPages = ceil(totalElements.toDouble() / size).toInt()

        val paginatedCourses =
            filteredCourses
                .drop(page * size)
                .take(size)

        val courseDtos = paginatedCourses.map { CourseDto(it) }

        return CoursePagingResponse(
            content = courseDtos,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages,
        )
    }
}
