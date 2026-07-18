package com.fillsa.app.api.oauth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import com.fillsa.app.common.exception.ApiErrorResponses
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode.*
import com.fillsa.service.member.Member
import com.fillsa.app.service.oauth.withdrawal.OAuthWithdrawalService
import java.net.URLEncoder

@RestController
@RequestMapping("/api/v1/oauth")
@Tag(name = "간편 로그인 콜백")
class OAuthCallbackController(
    private val oAuthWithdrawalService: OAuthWithdrawalService,
    @Value("\${fillsa.withdraw-url}")
    private val withdrawUrl: String
) {
    val log = KotlinLogging.logger {  }

    @ApiErrorResponses(
        INVALID_REQUEST,
        NOT_FOUND,
        WITHDRAWAL_USER
    )
    @GetMapping("/{provider}/callback")
    @Operation(summary = "[웹 탈퇴] 간편 로그인 콜백 api")
    fun kakaoOAuthCallback(
        @PathVariable provider: String,
        @RequestParam code: String,
        response: HttpServletResponse
    ) {
        try {
            oAuthWithdrawalService.withdraw(Member.OAuthProvider.fromPath(provider), code)
            response.sendRedirect("${withdrawUrl}/success")
        } catch (e: BusinessException) {
            log.error { "간편 로그인 콜백 businessException: $e" }
            response.sendRedirect("${withdrawUrl}/fail?message=${e.errorCode.code}")
        } catch (e: Exception) {
            log.error { "간편 로그인 콜백 exception: $e" }
            response.sendRedirect("${withdrawUrl}/fail?message=${URLEncoder.encode(e.message, "UTF-8")}")
        }
    }
}