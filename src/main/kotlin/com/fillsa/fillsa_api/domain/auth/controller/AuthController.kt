package com.fillsa.fillsa_api.domain.auth.controller

import com.fillsa.fillsa_api.domain.auth.service.useCase.AuthUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authUseCase: AuthUseCase
) {

    @PostMapping("/refresh")
    fun refreshToken() {}

    @PostMapping("/withdrawal")
    fun logout(
        @AuthenticationPrincipal member: Member
    ) {
        authUseCase.withdrawal(member)
    }
}