package com.wafflestudio.spring2025.sugangsnu.api

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SugangSnuApi(
    private val sugangSnuWebClient: WebClient,
) {
    fun get(): WebClient.RequestHeadersUriSpec<*> = sugangSnuWebClient.get()
}
