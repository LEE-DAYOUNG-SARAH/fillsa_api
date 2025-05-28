package store.fillsa.fillsa_api.domain.auth.service.auth

import io.jsonwebtoken.ExpiredJwtException
import org.springframework.stereotype.Service
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.common.security.JwtTokenProvider
import store.fillsa.fillsa_api.common.security.TokenInfo
import store.fillsa.fillsa_api.domain.auth.dto.*
import store.fillsa.fillsa_api.domain.auth.service.redis.RedisTokenService
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.service.MemberService
import store.fillsa.fillsa_api.domain.oauth.service.token.OAuthTokenService
import store.fillsa.fillsa_api.domain.oauth.service.withdrawal.OAuthWithdrawalService

@Service
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthWithdrawalService: OAuthWithdrawalService,
    private val oAuthTokenService: OAuthTokenService,
    private val memberService: MemberService,
    private val redisTokenService: RedisTokenService
) {
    fun login(
        provider: Member.OAuthProvider,
        tokenData: LoginRequest.TokenData,
        loginData: LoginRequest.LoginData
    ): Pair<Member, LoginResponse> {
        val member = memberService.signUp(provider, loginData)

        oAuthTokenService.createOAuthToken(member, tokenData)

        val token = createToken(member.memberSeq, tokenData.deviceId)

        return Pair(
            member,
            LoginResponse.from(token, member)
        )
    }

    private fun createToken(memberSeq: Long, deviceId: String): TokenInfo {
        val token = jwtTokenProvider.createTokens(memberSeq)
        redisTokenService.createRefreshToken(
            memberId = memberSeq,
            deviceId = deviceId,
            refreshToken = token.refreshToken,
            ttlMillis = jwtTokenProvider.refreshTokenValidity
        )

        return token
    }

    fun logout(member: Member, request: LogoutRequest) {
        redisTokenService.deleteRefreshToken(member.memberSeq, request.deviceId)
    }


    fun refreshToken(request: TokenRefreshRequest): TokenInfo {
        val memberSeq = jwtTokenProvider.getMemberSeqFromToken(request.refreshToken)

        validateRefreshToken(memberSeq, request)

        val member = memberService.getActiveMemberBySeq(memberSeq)
        return createToken(member.memberSeq, request.deviceId)
    }

    private fun validateRefreshToken(memberSeq: Long, request: TokenRefreshRequest) {
        try {
            jwtTokenProvider.validateToken(request.refreshToken)
        } catch (e: ExpiredJwtException) {
            throw BusinessException(JWT_REFRESH_TOKEN_EXPIRED)
        } catch (e: Exception) {
            throw BusinessException(JWT_REFRESH_TOKEN_INVALID)
        }

        if (!redisTokenService.validateRefreshToken(memberSeq, request.deviceId, request.refreshToken)) {
            throw BusinessException(REDIS_REFRESH_TOKEN_INVALID)
        }
    }

    fun withdraw(member: Member, request: WithdrawalRequest) {
        oAuthWithdrawalService.withdraw(member)

        memberService.withdraw(member)

        redisTokenService.deleteRefreshToken(member.memberSeq, request.deviceId)
    }
}