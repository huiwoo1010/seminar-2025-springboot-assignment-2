package com.wafflestudio.spring2025.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("SeminarSpring2025 API")
                    .version("1.0")
                    .description("Team11 Spring Boot REST API Document")
            )
    }
}