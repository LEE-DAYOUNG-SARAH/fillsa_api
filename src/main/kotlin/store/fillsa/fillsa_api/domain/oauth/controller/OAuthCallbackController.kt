package store.fillsa.fillsa_api.domain.oauth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import store.fillsa.fillsa_api.common.exception.ApiErrorResponses
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.service.withdrawal.OAuthWithdrawalService
import java.net.URLEncoder

@RestController
@RequestMapping("/api/v1/oauth")
@Tag(name = "간편 로그인 콜백")
class OAuthCallbackController(
    private val oAuthWithdrawalService: OAuthWithdrawalService
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
            response.sendRedirect("http://localhost:3000/success")
        } catch (e: Exception) {
            log.error { "간편 로그인 콜백 실패: $e" }
            response.sendRedirect("http://localhost:3000/fail?message=${URLEncoder.encode(e.message, "UTF-8")}")
        }
    }
}