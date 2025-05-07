package store.fillsa.fillsa_api.domain.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import store.fillsa.fillsa_api.domain.auth.dto.*
import store.fillsa.fillsa_api.common.security.TokenInfo
import store.fillsa.fillsa_api.domain.auth.service.auth.AuthService
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.service.MemberQuoteDataSyncService

@RestController
@RequestMapping("/auth")
@Tag(name = "권한", description = "권한 api")
class AuthController(
    private val authService: AuthService,
    private val memberQuoteDataSyncService: MemberQuoteDataSyncService
) {
    @PostMapping("/refresh")
    @Operation(summary = "토큰 발급 api")
    fun refreshToken(
        @RequestBody request: TokenRefreshRequest
    ): ResponseEntity<TokenInfo> = ResponseEntity.ok(
        authService.refreshToken(request)
    )

    @DeleteMapping("/withdraw")
    @Operation(summary = "탈퇴 api")
    fun logout(
        @AuthenticationPrincipal member: Member,
        @RequestBody request: WithdrawalRequest
    ) {
        authService.withdraw(member, request)
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃 api")
    fun logout(
        @AuthenticationPrincipal member: Member,
        @RequestBody request: LogoutRequest
    ) {
        authService.logout(member, request)
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 api")
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
}