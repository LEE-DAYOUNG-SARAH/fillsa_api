package com.fillsa.app.api.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import com.fillsa.app.common.exception.ApiErrorResponses
import com.fillsa.util.exception.ErrorCode.*
import com.fillsa.app.common.security.TokenInfo
import com.fillsa.app.api.auth.LoginRequest
import com.fillsa.app.api.auth.LoginResponse
import com.fillsa.app.api.auth.LogoutRequest
import com.fillsa.app.api.auth.TokenRefreshRequest
import com.fillsa.app.service.auth.AuthService
import com.fillsa.service.member.Member
import com.fillsa.app.service.members.quote.MemberQuoteDataSyncService

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "권한")
class AuthController(
    private val authService: AuthService,
    private val memberQuoteDataSyncService: MemberQuoteDataSyncService
) {
    @ApiErrorResponses(
        WITHDRAWAL_USER
    )
    @PostMapping("/login")
    @Operation(summary = "[1.login] 로그인 api")
    fun login(
        @RequestBody request: LoginRequest
    ): ResponseEntity<LoginResponse> {
        val (member, loginResponse) = authService.login(request.loginData)

        val syncScope = CoroutineScope(Dispatchers.Default)
        syncScope.launch {
            memberQuoteDataSyncService.syncData(member, request.syncData)
        }

        return ResponseEntity.ok(loginResponse)
    }

    @PostMapping("/logout")
    @Operation(summary = "[5. my page_login] 로그아웃 api")
    fun logout(
        @AuthenticationPrincipal member: Member,
        @RequestBody request: LogoutRequest
    ) {
        authService.logout(member, request)
    }

    @ApiErrorResponses(
        JWT_REFRESH_TOKEN_EXPIRED,
        JWT_REFRESH_TOKEN_INVALID
    )
    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급 api", description = "errorCode 3003인 경우 호출")
    fun refreshToken(
        @RequestBody request: TokenRefreshRequest
    ): ResponseEntity<TokenInfo> = ResponseEntity.ok(
        authService.refreshToken(request)
    )

    @DeleteMapping("/withdraw")
    @Operation(summary = "[modal_delete ID] 앱 탈퇴 api")
    fun withdraw(
        @AuthenticationPrincipal member: Member
    ) {
        authService.withdrawByApp(member)
    }
}