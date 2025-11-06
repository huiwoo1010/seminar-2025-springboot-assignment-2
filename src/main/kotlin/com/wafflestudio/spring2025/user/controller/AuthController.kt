package com.wafflestudio.spring2025.user.controller

import com.wafflestudio.spring2025.user.dto.LoginRequest
import com.wafflestudio.spring2025.user.dto.LoginResponse
import com.wafflestudio.spring2025.user.dto.RegisterRequest
import com.wafflestudio.spring2025.user.dto.RegisterResponse
import com.wafflestudio.spring2025.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "사용자 인증 관련 API")
class AuthController(
    private val userService: UserService,
) {
    @Operation(summary = "사용자 회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "회원가입 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = RegisterResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 데이터",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
    )
    @PostMapping("/register")
    fun register(
        @RequestBody registerRequest: RegisterRequest,
    ): ResponseEntity<RegisterResponse> {
        val userDto =
            userService.register(
                username = registerRequest.username,
                password = registerRequest.password,
            )
        return ResponseEntity.ok(userDto)
    }

    @Operation(summary = "사용자 로그인", description = "사용자 인증 후 JWT 토큰을 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = Schema(implementation = LoginResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패",
                content = [Content(mediaType = MediaType.APPLICATION_JSON_VALUE)],
            ),
        ],
    )
    @PostMapping("/login")
    fun login(
        @RequestBody loginRequest: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val token =
            userService.login(
                username = loginRequest.username,
                password = loginRequest.password,
            )
        return ResponseEntity.ok(LoginResponse(token))
    }
}
