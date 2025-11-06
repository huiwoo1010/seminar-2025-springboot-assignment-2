package com.wafflestudio.spring2025.user

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (isPublicPath(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }

        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val username = jwtTokenProvider.getUsername(token)
            request.setAttribute("username", username)
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing token")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }

    private fun isPublicPath(path: String): Boolean {
        val pathMatcher = AntPathMatcher()
        val publicPatterns =
            listOf(
                "/api/v1/auth/**",
                "/api/v1/import/**",
                "/api/v1/courses/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/error",
            )
        return publicPatterns.any { pattern -> pathMatcher.match(pattern, path) }
    }
}
