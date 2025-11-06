package com.wafflestudio.spring2025.sugangsnu.controller

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.sugangsnu.service.ImportResult
import com.wafflestudio.spring2025.sugangsnu.service.SugangSnuFetchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/import")
@Tag(name = "SugangSnu Import", description = "수강신청 웹사이트에서 강의 데이터 가져오기 API")
class SugangSnuFetchController(
    private val service: SugangSnuFetchService,
) {
    @Operation(summary = "강의 데이터 가져오기", description = "수강신청 웹사이트에서 강의 데이터를 가져와서 데이터베이스에 저장합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "데이터 가져오기 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = ImportResult::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 파라미터",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]
            ),
            ApiResponse(
                responseCode = "500",
                description = "수강신청 서버 오류",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)]
            ),
        ],
    )
    @PostMapping("/snu-courses")
    suspend fun import(
        @RequestParam year: Int,
        @RequestParam term: String,
        @RequestParam(defaultValue = "ko") lang: String,
        @RequestHeader(name = "Cookie", required = false) cookie: String?,
        @RequestHeader(name = "User-Agent", required = false) ua: String?,
        @RequestHeader(name = "Referer", required = false) referer: String?,
    ): ImportResult {
        val termEnum = Term.valueOf(term.uppercase())
        return service.importFromSugang(year, termEnum, lang, cookie, ua, referer)
    }
}
