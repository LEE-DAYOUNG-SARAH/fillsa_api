package com.fillsa.fillsa_api.domain.auth.service

import com.fillsa.fillsa_api.common.exception.InvalidRequestException
import com.fillsa.fillsa_api.domain.auth.dto.TokenRefreshRequest
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.auth.service.useCase.AuthUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import com.fillsa.fillsa_api.domain.oauth.service.OAuthServiceFactory
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val oauthServiceFactory: OAuthServiceFactory,
    private val memberUseCase: MemberUseCase
): AuthUseCase {

    override fun refreshToken(request: TokenRefreshRequest): TokenInfo {
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            throw InvalidRequestException("유효하지 않은 리프레시 토큰입니다.")
        }

        val member = memberUseCase.getActiveMemberBySeq(
            memberSeq = jwtTokenProvider.getMemberSeqFromToken(request.refreshToken)
        )

        return jwtTokenProvider.createTokens(member.memberSeq)
    }

    override fun withdrawal(member: Member) {
        val withdrawalService = oauthServiceFactory.getWithdrawalService(member.oauthProvider)
        withdrawalService.withdrawal(member)

        memberUseCase.withdrawal(member)
    }
}