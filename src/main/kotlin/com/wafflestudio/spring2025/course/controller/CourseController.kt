package com.wafflestudio.spring2025.course.controller

import com.wafflestudio.spring2025.course.dto.CoursePagingResponse
import com.wafflestudio.spring2025.course.service.CourseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
class CourseController(
    private val courseService: CourseService,
) {
    @GetMapping
    fun search(
        @RequestParam("year", required = true) year: Int,
        @RequestParam("semester", required = true) semester: String,
        @RequestParam("query", required = false) query: String?,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int,
    ): CoursePagingResponse = courseService.search(year, semester, query, page, size)
}
