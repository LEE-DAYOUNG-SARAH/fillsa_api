package com.fillsa.fillsa_api.domain.auth.service.auth

import com.fillsa.fillsa_api.common.exception.InvalidRequestException
import com.fillsa.fillsa_api.domain.auth.dto.*
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.auth.service.auth.useCase.AuthUseCase
import com.fillsa.fillsa_api.domain.auth.service.redis.useCase.RedisTokenUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import com.fillsa.fillsa_api.domain.oauth.service.withdrawal.OAuthWithdrawalService
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthWithdrawalService: OAuthWithdrawalService,
    private val memberUseCase: MemberUseCase,
    private val redisTokenUseCase: RedisTokenUseCase
): AuthUseCase {

    override fun refreshToken(request: TokenRefreshRequest): TokenInfo {
        val memberSeq = jwtTokenProvider.getMemberSeqFromToken(request.refreshToken)

        validateRefreshToken(memberSeq, request)

        val member = memberUseCase.getActiveMemberBySeq(memberSeq)
        return createToken(member.memberSeq, request.deviceId)
    }

    private fun validateRefreshToken(memberSeq: Long, request: TokenRefreshRequest) {
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw InvalidRequestException("유효하지 않은 리프레시 토큰")
        }

        if (!redisTokenUseCase.validateRefreshToken(memberSeq, request.deviceId, request.refreshToken)) {
            throw InvalidRequestException("유효하지 않은 리프레시 토큰")
        }
    }

    private fun createToken(memberSeq: Long, deviceId: String): TokenInfo {
        val token = jwtTokenProvider.createTokens(memberSeq)
        redisTokenUseCase.createRefreshToken(
            memberId = memberSeq,
            deviceId = deviceId,
            refreshToken = token.refreshToken,
            ttlMillis = jwtTokenProvider.refreshTokenValidity
        )

        return token
    }

    override fun withdrawal(member: Member, request: WithdrawalRequest) {
        oAuthWithdrawalService.withdraw(member)

        memberUseCase.withdraw(member)

        redisTokenUseCase.deleteRefreshToken(member.memberSeq, request.deviceId)
    }

    override fun logout(member: Member, request: LogoutRequest) {
        redisTokenUseCase.deleteRefreshToken(member.memberSeq, request.deviceId)
    }

    override fun login(request: TempTokenRequest): LoginResponse {
        val memberSeq = redisTokenUseCase.getAndDeleteTempToken(request.tempToken)?.toLong()
            ?: throw InvalidRequestException("만료되었거나 잘못된 임시 토큰입니다.")

        val member = memberUseCase.getActiveMemberBySeq(memberSeq)
        val token = createToken(memberSeq, request.deviceId)

        return LoginResponse(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            memberSeq = member.memberSeq,
            nickname = member.nickname.orEmpty(),
            profileImageUrl = member.profileImageUrl
        )
    }
}