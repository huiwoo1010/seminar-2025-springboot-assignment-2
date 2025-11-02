package com.wafflestudio.spring2025.config

import com.wafflestudio.spring2025.user.UserArgumentResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class WebConfig(
    private val userArgumentResolver: UserArgumentResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(userArgumentResolver)
    }

    @Bean(name = ["SugangSnuWebClient"])
    fun sugangSnuWebClient(): WebClient {
        val strategies =
            ExchangeStrategies
                .builder()
                .codecs { it.defaultCodecs().maxInMemorySize(20 * 1024 * 1024) }
                .build()

        val httpClient =
            HttpClient
                .create()
                .responseTimeout(Duration.ofSeconds(30))

        return WebClient
            .builder()
            .baseUrl("https://sugang.snu.ac.kr")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies)
            .defaultHeader("Accept", "*/*")
            .build()
    }
}
