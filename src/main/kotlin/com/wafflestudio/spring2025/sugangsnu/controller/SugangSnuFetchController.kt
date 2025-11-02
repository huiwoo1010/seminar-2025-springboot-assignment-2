package com.wafflestudio.spring2025.sugangsnu.controller

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.sugangsnu.service.SugangSnuFetchService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class ImportResult(
    val imported: Int,
    val updated: Int,
)

@RestController
@RequestMapping("/api/v1/import")
class SugangSnuFetchController(
    private val service: SugangSnuFetchService,
) {
    @PostMapping("/snu-courses")
    suspend fun import(
        @RequestParam year: Int,
        @RequestParam term: Term,
        @RequestParam(required = false, defaultValue = "ko") lang: String,
    ): ImportResult = service.importFromSugang(year, term, lang)
}
