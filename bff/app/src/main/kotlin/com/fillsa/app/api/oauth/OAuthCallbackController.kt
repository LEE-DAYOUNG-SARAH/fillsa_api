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
import java.nio.charset.StandardCharsets

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
        // required=false — 필수로 두면 code 누락 시 컨트롤러 진입 전에 예외가 나서
        // 아래 catch 를 타지 못하고 500 이 그대로 사용자에게 노출된다.
        @RequestParam(required = false) code: String?,
        response: HttpServletResponse
    ) {
        try {
            if (code.isNullOrBlank()) {
                throw BusinessException(INVALID_REQUEST, "code 누락")
            }
            oAuthWithdrawalService.withdraw(Member.OAuthProvider.fromPath(provider), code)
            response.sendRedirect("${withdrawUrl}/success")
        } catch (e: BusinessException) {
            log.error { "간편 로그인 콜백 businessException: $e" }
            response.sendRedirect(failUrl(e.errorCode.code.toString()))
        } catch (e: Exception) {
            log.error(e) { "간편 로그인 콜백 exception" }
            // e.message 가 null 인 예외(NPE 등)에서 URLEncoder 가 다시 터지지 않게 한다.
            // 원시 예외 메시지를 쿼리스트링에 노출하지 않고 공통 에러코드로 대체한다.
            response.sendRedirect(failUrl(UNEXPECTED_EXCEPTION.code.toString()))
        }
    }

    private fun failUrl(message: String) =
        "$withdrawUrl/fail?message=${URLEncoder.encode(message, StandardCharsets.UTF_8)}"
}