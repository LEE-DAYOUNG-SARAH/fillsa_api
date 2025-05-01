package com.fillsa.fillsa_api.domain.auth.controller

import com.fillsa.fillsa_api.domain.auth.dto.*
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.auth.service.auth.useCase.AuthUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.service.useCase.MemberQuoteDataSyncUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
@Tag(name = "권한", description = "권한 api")
class AuthController(
    private val authUseCase: AuthUseCase,
    private val memberQuoteDataSyncUseCase: MemberQuoteDataSyncUseCase
) {
    @PostMapping("/refresh")
    @Operation(summary = "토큰 발급 api")
    fun refreshToken(
        @RequestBody request: TokenRefreshRequest
    ): ResponseEntity<TokenInfo> = ResponseEntity.ok(
        authUseCase.refreshToken(request)
    )

    @DeleteMapping("/withdraw")
    @Operation(summary = "탈퇴 api")
    fun logout(
        @AuthenticationPrincipal member: Member,
        @RequestBody request: WithdrawalRequest
    ) {
        authUseCase.withdraw(member, request)
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 api")
    fun logout(
        @AuthenticationPrincipal member: Member,
        @RequestBody request: LogoutRequest
    ) {
        authUseCase.logout(member, request)
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 api")
    fun login(
        @RequestBody request: LoginRequest
    ): ResponseEntity<LoginResponse> {
        val (member, loginResponse) = authUseCase.login(request.loginData)

        val syncScope = CoroutineScope(Dispatchers.Default)
        syncScope.launch {
            memberQuoteDataSyncUseCase.syncData(member, request.syncData)
        }

        return ResponseEntity.ok(loginResponse)
    }
}