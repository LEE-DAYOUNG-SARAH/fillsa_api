package com.fillsa.fillsa_api.domain.auth.controller

import com.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.service.useCase.AuthUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val kakaoAuthService: AuthUseCase
) {

    @PostMapping("/kakao/login")
    fun kakaoLogin(
        @RequestBody request: LoginRequest
    ): ResponseEntity<LoginResponse> = ResponseEntity.ok(
        kakaoAuthService.login(request)
    )
}