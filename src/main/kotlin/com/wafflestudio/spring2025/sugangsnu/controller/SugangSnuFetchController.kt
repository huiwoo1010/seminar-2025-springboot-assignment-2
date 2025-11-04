package com.wafflestudio.spring2025.sugangsnu.controller

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.sugangsnu.service.ImportResult
import com.wafflestudio.spring2025.sugangsnu.service.SugangSnuFetchService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/import")
class SugangSnuFetchController(
    private val service: SugangSnuFetchService,
) {
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
