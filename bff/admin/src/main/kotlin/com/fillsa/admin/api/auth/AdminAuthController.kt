package com.fillsa.admin.api.auth

import com.fillsa.admin.service.auth.AdminAuthService
import com.fillsa.admin.service.auth.LoginRequest
import com.fillsa.admin.service.auth.RefreshRequest
import com.fillsa.admin.service.auth.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/v1/auth")
@Tag(name = "auth", description = "어드민 인증")
class AdminAuthController(
    private val adminAuthService: AdminAuthService,
) {

    @PostMapping("/login")
    @Operation(summary = "어드민 로그인")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(adminAuthService.login(request))

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 (토큰 무효화)")
    fun logout(): ResponseEntity<Void> {
        // 무상태(stateless) 토큰이므로 서버 저장소 무효화는 없다. 클라이언트가 토큰을 폐기한다.
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<TokenResponse> =
        ResponseEntity.ok(adminAuthService.refresh(request))
}
