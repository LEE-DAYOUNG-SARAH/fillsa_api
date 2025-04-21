package com.fillsa.fillsa_api.domain.auth.controller

import com.fillsa.fillsa_api.domain.auth.service.KakaoOAuthService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class OAuthController(
    private val kakaoOAuthService: KakaoOAuthService,
) {
    @GetMapping("/kakao/callback")
    fun kakaoCallback(
        @RequestParam code: String,
        response: HttpServletResponse
    ) {
        try {
            val loginResponse = kakaoOAuthService.processOauthCallback(code)
            response.sendRedirect("fillsa://oauth/callback?temp_token=${loginResponse.accessToken}")
        } catch (e: Exception) {
            response.sendRedirect("fillsa://oauth/error?message=${e.message}")
        }
    }
}