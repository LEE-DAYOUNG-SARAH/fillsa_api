package com.fillsa.fillsa_api.domain.oauth.controller

import com.fillsa.fillsa_api.domain.auth.service.redis.useCase.RedisTokenUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.service.callback.OAuthCallbackService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/oauth")
@Tag(name = "로그인", description = "로그인 콜백 api")
class OAuthCallbackController(
    private val oAuthCallbackService: OAuthCallbackService,
    private val redisTokenUseCase: RedisTokenUseCase
) {

    @GetMapping("/{provider}/callback")
    @Operation(summary = "간편 로그인 콜백 api")
    fun oauthCallback(
        @Parameter(description = "OAuth Provider(google/kakao)")
        @PathVariable provider: String,
        @RequestParam code: String,
        response: HttpServletResponse
    ) {
        try {
            val memberSeq = oAuthCallbackService.processOAuthCallback(
                provider = Member.OAuthProvider.fromPath(provider),
                code = code
            )

            val tempToken = UUID.randomUUID().toString()
            redisTokenUseCase.createTempToken(tempToken, memberSeq, 5 * 60 * 1000)

            response.sendRedirect("fillsa://oauth/callback?temp_token=$tempToken")
        } catch (e: Exception) {
            response.sendRedirect("fillsa://oauth/error?message=${e.message}")
        }
    }
}