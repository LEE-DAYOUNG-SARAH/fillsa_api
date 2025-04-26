package com.fillsa.fillsa_api.domain.oauth.controller

import com.fillsa.fillsa_api.domain.oauth.service.OAuthServiceFactory
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class OAuthCallbackController(
    private val oAuthServiceFactory: OAuthServiceFactory
) {
    // TODO. redirect 딥링크 앱이랑 상의
    // TODO. redis 붙여서 tempToken 보내기

    @GetMapping("/kakao/callback")
    fun kakaoCallback(
        @RequestParam code: String,
        response: HttpServletResponse
    ) {
        try {
            val loginResponse = oAuthServiceFactory.getCallbackService(Member.OAuthProvider.KAKAO)
                .processOAuthCallback(code)
            response.sendRedirect("fillsa://oauth/callback?temp_token=${loginResponse.accessToken}")
        } catch (e: Exception) {
            response.sendRedirect("fillsa://oauth/error?message=${e.message}")
        }
    }

    @GetMapping("/google/callback")
    fun googleCallback(
        @RequestParam code: String,
        response: HttpServletResponse
    ) {
        try {
            val loginResponse = oAuthServiceFactory.getCallbackService(Member.OAuthProvider.GOOGLE)
                .processOAuthCallback(code)
            response.sendRedirect("fillsa://oauth/callback?temp_token=${loginResponse.accessToken}")
        } catch (e: Exception) {
            response.sendRedirect("fillsa://oauth/error?message=${e.message}")
        }
    }
}