package com.wafflestudio.spring2025.course.controller

import com.wafflestudio.spring2025.course.dto.CoursePagingResponse
import com.wafflestudio.spring2025.course.service.CourseService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/courses")
@Tag(name = "Course", description = "강의 검색 관련 API")
class CourseController(
    private val courseService: CourseService,
) {
    @Operation(summary = "강의 검색", description = "연도, 학기, 검색어로 강의를 검색합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "강의 검색 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = CoursePagingResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]
            ),
        ],
    )
    @GetMapping
    fun search(
        @RequestParam("year", required = true) year: Int,
        @RequestParam("semester", required = true) semester: String,
        @RequestParam("query", required = false) query: String?,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "20") size: Int,
    ): CoursePagingResponse = courseService.search(year, semester, query, page, size)
}
