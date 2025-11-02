package com.wafflestudio.spring2025.sugangsnu.repository

import com.wafflestudio.spring2025.common.Term
import com.wafflestudio.spring2025.sugangsnu.util.SugangSnuUrlUtils.toSugangCode
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.buffer.PooledDataBuffer
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SugangSnuRepository(
    @Qualifier("SugangSnuWebClient")
    private val client: WebClient,
) {
    private val excelPath = "/sugang/cc/cc100InterfaceExcel.action"

    private val defaultExcelQuery =
        "seeMore=더보기&srchBdNo=&srchCamp=&srchOpenSbjtFldCd=&srchCptnCorsFg=&" +
            "srchCurrPage=1&srchExcept=&srchGenrlRemoteLtYn=&srchIsEngSbjt=&" +
            "srchIsPendingCourse=&srchLsnProgType=&srchMrksApprMthdChgPosbYn=&srchMrksGvMthd=&" +
            "srchOpenUpDeptCd=&srchOpenMjCd=&srchOpenPntMax=&srchOpenPntMin=&srchOpenSbjtDayNm=&" +
            "srchOpenSbjtNm=&srchOpenSbjtTm=&srchOpenSbjtTmNm=&srchOpenShyr=&srchOpenSubmattCorsFg=&" +
            "srchOpenSubmattFgCd1=&srchOpenSubmattFgCd2=&srchOpenSubmattFgCd3=&srchOpenSubmattFgCd4=&" +
            "srchOpenSubmattFgCd5=&srchOpenSubmattFgCd6=&srchOpenSubmattFgCd7=&srchOpenSubmattFgCd8=&" +
            "srchOpenSubmattFgCd9=&srchOpenDeptCd=&srchOpenUpSbjtFldCd=&srchPageSize=9999&" +
            "srchProfNm=&srchSbjtCd=&srchSbjtNm=&srchTlsnAplyCapaCntMax=&srchTlsnAplyCapaCntMin=&" +
            "srchTlsnRcntMax=&srchTlsnRcntMin=&workType=EX"

    suspend fun downloadLecturesXls(
        year: Int,
        term: Term,
        lang: String,
        cookie: String? = null,
        ua: String? = null,
        referer: String? = null,
    ): PooledDataBuffer =
        client
            .get()
            .uri { b ->
                b
                    .path(excelPath)
                    .query(defaultExcelQuery)
                    .queryParam("srchLanguage", lang)
                    .queryParam("srchOpenSchyy", year)
                    .queryParam("srchOpenShtm", toSugangCode(term))
                    .build()
            }.headers { h ->
                if (!cookie.isNullOrBlank()) h.add("Cookie", cookie)
                if (!ua.isNullOrBlank()) h.add("User-Agent", ua)
                if (!referer.isNullOrBlank()) h.add("Referer", referer)
            }
            // 서버가 종종 content-type을 text/html로 내려도 실제는 바이너리일 수 있음
            .accept(MediaType.TEXT_HTML)
            .retrieve()
            .bodyToMono(PooledDataBuffer::class.java)
            .awaitSingle()
}
