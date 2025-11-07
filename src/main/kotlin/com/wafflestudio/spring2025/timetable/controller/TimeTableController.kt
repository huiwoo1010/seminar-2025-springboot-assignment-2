package com.wafflestudio.spring2025.timetable.controller

import com.wafflestudio.spring2025.timetable.dto.AddCourseRequest
import com.wafflestudio.spring2025.timetable.dto.CreateTimeTableRequest
import com.wafflestudio.spring2025.timetable.dto.UpdateTimeTableRequest
import com.wafflestudio.spring2025.timetable.dto.core.TimeTableDetailDto
import com.wafflestudio.spring2025.timetable.dto.core.TimeTableDto
import com.wafflestudio.spring2025.timetable.service.TimeTableService
import com.wafflestudio.spring2025.user.LoggedInUser
import com.wafflestudio.spring2025.user.model.User
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
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
@Tag(name = "TimeTable", description = "시간표 관련 API")
class TimeTableController(
    private val timetableService: TimeTableService,
) {
    @Operation(summary = "시간표 생성", description = "새로운 시간표를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "시간표 생성 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = TimeTableDto::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    fun create(
        @RequestBody createRequest: CreateTimeTableRequest,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): TimeTableDto = timetableService.create(createRequest.name, createRequest.year, createRequest.semester, user)

    @Operation(summary = "시간표 목록 조회", description = "내 모든 시간표 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "시간표 목록 조회 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = TimeTableDto::class, type = "array"),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    fun list(
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): List<TimeTableDto> = timetableService.list(user)

    @Operation(summary = "시간표 상세 조회", description = "특정 시간표의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "시간표 상세 조회 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = TimeTableDetailDto::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시간표를 찾을 수 없음",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    fun detail(
        @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): TimeTableDetailDto = timetableService.detail(id, user)

    @Operation(summary = "시간표 수정", description = "시간표의 이름을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "시간표 수정 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = TimeTableDto::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시간표를 찾을 수 없음",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody updateRequest: UpdateTimeTableRequest,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): TimeTableDto = timetableService.update(id, updateRequest.name, user)

    @Operation(summary = "시간표 삭제", description = "시간표를 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "시간표 삭제 성공"),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시간표를 찾을 수 없음",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): ResponseEntity<Unit> {
        timetableService.delete(id, user)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "시간표에 강의 추가", description = "시간표에 강의를 추가합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "강의 추가 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = TimeTableDetailDto::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시간표 또는 강의를 찾을 수 없음",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
            ApiResponse(responseCode = "409", description = "시간대 겹침", content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{timeTableId}/courses")
    fun addCourse(
        @PathVariable timeTableId: Long,
        @RequestBody addCourseRequest: AddCourseRequest,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): TimeTableDetailDto = timetableService.addCourse(timeTableId, addCourseRequest.courseId, user)

    @Operation(summary = "시간표에서 강의 제거", description = "시간표에서 강의를 제거합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "강의 제거 성공",
                content = [
                    Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = Schema(implementation = TimeTableDetailDto::class),
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증되지 않은 사용자",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
            ApiResponse(
                responseCode = "404",
                description = "시간표 또는 강의를 찾을 수 없음",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{timeTableId}/courses/{courseId}")
    fun removeCourse(
        @PathVariable timeTableId: Long,
        @PathVariable courseId: Long,
        @Parameter(hidden = true) @LoggedInUser user: User,
    ): TimeTableDetailDto = timetableService.removeCourse(timeTableId, courseId, user)
}
