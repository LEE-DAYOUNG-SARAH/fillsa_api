package store.fillsa.fillsa_api.domain.auth.service.auth

import io.jsonwebtoken.ExpiredJwtException
import org.springframework.stereotype.Service
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.common.security.JwtTokenProvider
import store.fillsa.fillsa_api.common.security.TokenInfo
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import store.fillsa.fillsa_api.domain.auth.dto.LogoutRequest
import store.fillsa.fillsa_api.domain.auth.dto.TokenRefreshRequest
import store.fillsa.fillsa_api.domain.auth.service.redis.RedisTokenService
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.service.MemberDeviceService
import store.fillsa.fillsa_api.domain.members.member.service.MemberService

@Service
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberService: MemberService,
    private val memberDeviceService: MemberDeviceService,
    private val redisTokenService: RedisTokenService
) {
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
        redisTokenService.createRefreshToken(
            memberId = memberSeq,
            deviceId = deviceId,
            refreshToken = token.refreshToken,
            ttlMillis = jwtTokenProvider.refreshTokenValidity
        )

        return token
    }

    fun logout(member: Member, request: LogoutRequest) {
        redisTokenService.deleteRefreshTokenForLogout(member.memberSeq, request.deviceId)

        memberDeviceService.logout(member, request.deviceId)
    }

    fun refreshToken(request: TokenRefreshRequest): TokenInfo {
        validateRefreshToken(request.refreshToken)

        val memberSeq = jwtTokenProvider.getMemberSeqFromToken(request.refreshToken)

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

    fun withdrawByApp(member: Member) {
        memberService.withdraw(member)

        redisTokenService.deleteRefreshTokenForWithdrawal(member.memberSeq)
    }

    fun withdrawByWeb(oauthId: String, provider: Member.OAuthProvider) {
        val members = memberService.getAllMemberByOauthId(oauthId, provider)
        if(members.isEmpty()) throw BusinessException(NOT_FOUND)

        val activeMember = members.find { !it.isWithdrawal() } ?: throw BusinessException(WITHDRAWAL_USER)

        withdrawByApp(activeMember)
    }
}