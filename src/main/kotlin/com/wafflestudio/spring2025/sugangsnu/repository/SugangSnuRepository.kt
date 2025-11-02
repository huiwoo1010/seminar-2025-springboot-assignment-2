package com.wafflestudio.spring2025.sugangsnu.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.sugangsnu.api.SugangSnuApi
import com.wafflestudio.spring2025.sugangsnu.util.SugangSnuUrlUtils.convertSemesterToSugangSnuSearchString
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.createExceptionAndAwait

@Component
class SugangSnuRepository(
    private val sugangSnuApi: SugangSnuApi,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        const val SUGANG_SNU_COURSEBOOK_PATH = "/sugang/cc/cc100ajax.action"
        const val DEFAULT_COURSEBOOK_PARAMS = "openUpDeptCd=&openDeptCd="
        const val SUGANG_SNU_SEARCH_PATH = "/sugang/cc/cc100InterfaceSrch.action"
        const val DEFAULT_SEARCH_PAGE_PARAMS = "workType=S&sortKey=&sortOrder="
        const val SUGANG_SNU_SEARCH_POPUP_PATH = "/sugang/cc/cc101ajax.action"
        const val DEFAULT_SEARCH_POPUP_PARAMS = """t_profPersNo=&workType=+&sbjtSubhCd=000"""
        const val SUGANG_SNU_LECTURE_EXCEL_DOWNLOAD_PATH = "/sugang/cc/cc100InterfaceExcel.action"
        val DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS =
            """
            seeMore=더보기&
            srchBdNo=&srchCamp=&srchOpenSbjtFldCd=&srchCptnCorsFg=&
            srchCurrPage=1&
            srchExcept=&srchGenrlRemoteLtYn=&srchIsEngSbjt=&
            srchIsPendingCourse=&srchLsnProgType=&srchMrksApprMthdChgPosbYn=&srchMrksGvMthd=&
            srchOpenUpDeptCd=&srchOpenMjCd=&srchOpenPntMax=&srchOpenPntMin=&srchOpenSbjtDayNm=&
            srchOpenSbjtNm=&srchOpenSbjtTm=&srchOpenSbjtTmNm=&srchOpenShyr=&srchOpenSubmattCorsFg=&
            srchOpenSubmattFgCd1=&srchOpenSubmattFgCd2=&srchOpenSubmattFgCd3=&srchOpenSubmattFgCd4=&
            srchOpenSubmattFgCd5=&srchOpenSubmattFgCd6=&srchOpenSubmattFgCd7=&srchOpenSubmattFgCd8=&
            srchOpenSubmattFgCd9=&srchOpenDeptCd=&srchOpenUpSbjtFldCd=&
            srchPageSize=9999&
            srchProfNm=&srchSbjtCd=&srchSbjtNm=&srchTlsnAplyCapaCntMax=&srchTlsnAplyCapaCntMin=&srchTlsnRcntMax=&srchTlsnRcntMin=&
            workType=EX
            """.trimIndent().replace("\n", "")
    }

    suspend fun getSearchPageHtml(pageNo: Int = 1): PooledDataBuffer =
        sugangSnuApi
            .get()
            .uri { builder ->
                builder
                    .path(SUGANG_SNU_SEARCH_PATH)
                    .query(DEFAULT_SEARCH_PAGE_PARAMS)
                    .queryParam("pageNo", pageNo)
                    .build()
            }.accept(MediaType.TEXT_HTML)
            .retrieve()
            .awaitBody()

    // 필요하면 팝업 JSON 파싱용 dto 추가
    suspend fun getLectureInfo(
        year: Int,
        term: Term,
        courseNumber: String,
        lectureNumber: String,
    ): Map<String, Any?> =
        sugangSnuApi
            .get()
            .uri { builder ->
                val sem = convertSemesterToSugangSnuSearchString(year, term)
                builder
                    .path(SUGANG_SNU_SEARCH_POPUP_PATH)
                    .query(DEFAULT_SEARCH_POPUP_PARAMS)
                    .queryParam("openSchyy", year)
                    // SNUTT는 문자열을 0..9 / 10.. 으로 쪼개서 두 파라미터로 보냄
                    .queryParam("openShtmFg", sem.substring(0..9))
                    .queryParam("openDetaShtmFg", sem.substring(10))
                    .queryParam("sbjtCd", courseNumber)
                    .queryParam("ltNo", lectureNumber)
                    .build()
            }.accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .awaitBody()

    suspend fun getSugangSnuLectures(
        year: Int,
        term: Term,
        language: String = "ko",
    ): PooledDataBuffer =
        sugangSnuApi
            .get()
            .uri { builder ->
                val sem = convertSemesterToSugangSnuSearchString(year, term)
                builder.run {
                    path(SUGANG_SNU_LECTURE_EXCEL_DOWNLOAD_PATH)
                    query(DEFAULT_LECTURE_EXCEL_DOWNLOAD_PARAMS)
                    queryParam("srchLanguage", language)
                    queryParam("srchOpenSchyy", year)
                    queryParam("srchOpenShtm", sem)
                    build()
                }
            }.accept(MediaType.TEXT_HTML) // 실제 응답은 엑셀 바이너리지만 컨텐츠타입이 다양해서 관대하게
            .awaitExchange {
                if (it.statusCode().is2xxSuccessful) {
                    it.awaitBody()
                } else {
                    throw it.createExceptionAndAwait()
                }
            }
}
