package store.fillsa.fillsa_api.domain.oauth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*
import store.fillsa.fillsa_api.common.exception.ApiErrorResponses
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.domain.auth.service.redis.RedisTokenService
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.service.callback.OAuthCallbackService
import java.util.*

@RestController
@RequestMapping("/api/v1/oauth")
@Tag(name = "간편 로그인")
class OAuthCallbackController(
    private val oAuthCallbackService: OAuthCallbackService,
    private val redisTokenService: RedisTokenService
) {
    val log = KotlinLogging.logger {  }

    @ApiErrorResponses(
        INVALID_REQUEST,
        OAUTH_TOKEN_REQUEST_FAILED,
        OAUTH_TOKEN_RESPONSE_PROCESS_FAILED,
        OAUTH_USER_REQUEST_FAILED,
        OAUTH_USER_RESPONSE_PROCESS_FAILED
    )
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
            log.info { "tempToken: [$tempToken]" }

            redisTokenService.createTempToken(tempToken, memberSeq, 5 * 60 * 1000)

            response.sendRedirect("fillsa://oauth/callback?temp_token=$tempToken")
        } catch (e: Exception) {
            log.error { "OAuth2 콜백 실패: $e" }
            response.sendRedirect("fillsa://oauth/error?message=${e.message}")
        }
    }
}