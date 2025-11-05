package com.wafflestudio.spring2025.course.controller

import com.wafflestudio.spring2025.course.dto.corre.CourseDto
import com.wafflestudio.spring2025.course.service.CourseService
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class CourseController(
    private val courseService: CourseService,
) {
    @GetMapping("/course")
    fun get(
        @RequestParam("query", required = false) query: String?,
    )
: List<CourseDto> {
        val courses = courseService.get(query)
        return courses.map { CourseDto(it) }
    }
}