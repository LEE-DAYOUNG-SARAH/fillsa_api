package com.fillsa.fillsa_api.domain.auth.controller

import com.fillsa.fillsa_api.domain.auth.dto.TokenRefreshRequest
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.auth.service.useCase.AuthUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
@Tag(name = "권한", description = "권한 api")
class AuthController(
    private val authUseCase: AuthUseCase
) {

    @PostMapping("/refresh")
    @Operation(summary = "토큰 발급 api")
    fun refreshToken(
        @RequestBody request: TokenRefreshRequest
    ): ResponseEntity<TokenInfo> = ResponseEntity.ok(
        authUseCase.refreshToken(request)
    )

    @PostMapping("/withdrawal")
    @Operation(summary = "탈퇴 api")
    fun logout(
        @AuthenticationPrincipal member: Member
    ) {
        authUseCase.withdrawal(member)
    }
}