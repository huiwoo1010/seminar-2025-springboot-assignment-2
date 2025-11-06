package com.wafflestudio.spring2025.timetable.controller

import com.wafflestudio.spring2025.timetable.dto.AddCourseRequest
import com.wafflestudio.spring2025.timetable.dto.CreateTimeTableRequest
import com.wafflestudio.spring2025.timetable.dto.CreateTimeTableResponse
import com.wafflestudio.spring2025.timetable.dto.UpdateTimeTableRequest
import com.wafflestudio.spring2025.timetable.dto.core.TimeTableDetailDto
import com.wafflestudio.spring2025.timetable.dto.core.TimeTableDto
import com.wafflestudio.spring2025.timetable.service.TimeTableService
import com.wafflestudio.spring2025.user.LoggedInUser
import com.wafflestudio.spring2025.user.model.User
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/timetable")
class TimeTableController(
    private val timetableService: TimeTableService,
) {
    @PostMapping
    fun create(
        @RequestBody createRequest: CreateTimeTableRequest,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<CreateTimeTableResponse> {
        val timetable = timetableService.create(createRequest.name, createRequest.year, createRequest.semester, user)
        return ResponseEntity.ok(timetable)
    }

    @GetMapping
    fun list(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<List<TimeTableDto>> {
        val timetables = timetableService.list(user)
        return ResponseEntity.ok(timetables)
    }

    @GetMapping("/{id}")
    fun detail(
        @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<TimeTableDetailDto> {
        val timetableDetail = timetableService.detail(id, user)
        return ResponseEntity.ok(timetableDetail)
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody updateRequest: UpdateTimeTableRequest,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<TimeTableDto> {
        val updatedTimeTable = timetableService.update(id, updateRequest.name, user)
        return ResponseEntity.ok(updatedTimeTable)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<Unit> {
        timetableService.delete(id, user)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{timeTableId}/courses")
    fun addCourse(
        @PathVariable timeTableId: Long,
        @RequestBody addCourseRequest: AddCourseRequest,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<TimeTableDetailDto> {
        val updatedTimeTableDetail = timetableService.addCourse(timeTableId, addCourseRequest.courseId, user)
        return ResponseEntity.ok(updatedTimeTableDetail)
    }

    @DeleteMapping("/{timeTableId}/courses/{courseId}")
    fun removeCourse(
        @PathVariable timeTableId: Long,
        @PathVariable courseId: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<TimeTableDetailDto> {
        val updatedTimeTableDetail = timetableService.removeCourse(timeTableId, courseId, user)
        return ResponseEntity.ok(updatedTimeTableDetail)
    }
}
