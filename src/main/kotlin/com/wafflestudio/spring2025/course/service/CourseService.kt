package com.wafflestudio.spring2025.course.service

import com.wafflestudio.spring2025.course.model.Course
import com.wafflestudio.spring2025.course.repository.CourseRepository
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseRepository: CourseRepository,
) {
    fun get(query: String?): List<Course> {
        return if (query.isNullOrBlank()) {
            courseRepository.findAll().toList()
        } else {
            courseRepository.findByTitleContainingIgnoreCaseOrProfessorContainingIgnoreCase(query, query)
        }
    }
}