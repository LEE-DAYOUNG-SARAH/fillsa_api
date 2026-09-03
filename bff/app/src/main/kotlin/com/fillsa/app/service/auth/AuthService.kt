package com.fillsa.app.service.auth

import io.jsonwebtoken.ExpiredJwtException
import org.springframework.stereotype.Service
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode.*
import com.fillsa.app.common.security.JwtTokenProvider
import com.fillsa.app.common.security.TokenInfo
import com.fillsa.app.api.auth.LoginRequest
import com.fillsa.app.api.auth.LoginResponse
import com.fillsa.app.api.auth.LogoutRequest
import com.fillsa.app.api.auth.TokenRefreshRequest
import com.fillsa.app.common.redis.service.RefreshTokenCacheService
import com.fillsa.service.member.Member
import com.fillsa.app.api.oauth.client.apple.AppleAuthClient
import com.fillsa.app.service.members.member.MemberDeviceService
import com.fillsa.app.service.members.member.MemberService
import mu.KotlinLogging

@Service
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberService: MemberService,
    private val memberDeviceService: MemberDeviceService,
    private val refreshTokenCacheService: RefreshTokenCacheService,
    private val appleAuthClient: AppleAuthClient,
) {
    private val log = KotlinLogging.logger { }

    fun login(request: LoginRequest.LoginData): Pair<Member, LoginResponse> {
        val member = memberService.signUp(request)
        val token = createToken(member.memberSeq, request.deviceData.deviceId)

        return Pair(
            member,
            LoginResponse.from(token, member)
        )
    }

    private fun createToken(memberSeq: Long, deviceId: String): TokenInfo {
        val token = jwtTokenProvider.createTokens(memberSeq)
        refreshTokenCacheService.createRefreshToken(
            memberId = memberSeq,
            deviceId = deviceId,
            refreshToken = token.refreshToken,
            ttlMillis = jwtTokenProvider.refreshTokenValidity
        )

        return token
    }

    fun logout(member: Member, request: LogoutRequest) {
        refreshTokenCacheService.deleteRefreshTokenForLogout(member.memberSeq, request.deviceId)

        memberDeviceService.logout(member, request.deviceId)
    }

    fun refreshToken(request: TokenRefreshRequest): TokenInfo {
        validateRefreshToken(request.refreshToken)

        val memberSeq = jwtTokenProvider.getMemberSeqFromToken(request.refreshToken)

        // 저장된 토큰과 대조한다. 서명·만료만 보면 로그아웃한 토큰도 유효기간 내내 재발급에 쓸 수 있다.
        if (!refreshTokenCacheService.isValidRefreshToken(memberSeq, request.deviceId, request.refreshToken)) {
            throw BusinessException(JWT_REFRESH_TOKEN_INVALID)
        }

        val member = memberService.getActiveMemberBySeq(memberSeq)
        return createToken(member.memberSeq, request.deviceId)
    }

    private fun validateRefreshToken(refreshToken: String) {
        try {
            jwtTokenProvider.validateToken(refreshToken)
        } catch (e: ExpiredJwtException) {
            throw BusinessException(JWT_REFRESH_TOKEN_EXPIRED)
        } catch (e: Exception) {
            throw BusinessException(JWT_REFRESH_TOKEN_INVALID)
        }
    }

    fun withdrawByApp(member: Member, appleAuthorizationCode: String? = null) {
        // 앱스토어 심사 요건: Apple 회원 탈퇴 시 토큰 폐기. 폐기 실패가 탈퇴 자체를 막지는 않는다(best-effort).
        if (member.oauthProvider == Member.OAuthProvider.APPLE && !appleAuthorizationCode.isNullOrBlank()) {
            runCatching { appleAuthClient.revoke(appleAuthorizationCode) }
                .onFailure { log.warn(it) { "Apple 토큰 폐기 실패 — 탈퇴는 계속 진행 (memberSeq=${member.memberSeq})" } }
        } else if (member.oauthProvider == Member.OAuthProvider.APPLE) {
            log.warn { "Apple 회원 탈퇴에 authorizationCode 미전달 — revoke 생략 (memberSeq=${member.memberSeq})" }
        }

        memberService.withdraw(member)

        refreshTokenCacheService.deleteRefreshTokenForWithdrawal(member.memberSeq)
    }

    fun withdrawByWeb(oauthId: String, provider: Member.OAuthProvider) {
        val members = memberService.getAllMemberByOauthId(oauthId, provider)
        if(members.isEmpty()) throw BusinessException(NOT_FOUND)

        val activeMember = members.find { !it.isWithdrawal() } ?: throw BusinessException(WITHDRAWAL_USER)

        withdrawByApp(activeMember)
    }
}