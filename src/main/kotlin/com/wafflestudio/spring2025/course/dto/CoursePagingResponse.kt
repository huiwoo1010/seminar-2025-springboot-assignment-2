package com.wafflestudio.spring2025.course.dto

import com.wafflestudio.spring2025.course.dto.corre.CourseDto

data class CoursePagingResponse(
    val content: List<CourseDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
