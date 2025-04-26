package com.fillsa.fillsa_api.domain.auth.service

import com.fillsa.fillsa_api.common.exception.InvalidRequestException
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
    // 토큰 갱신
    override fun refreshToken(refreshToken: String): TokenInfo {
        // 1. 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw InvalidRequestException("유효하지 않은 리프레시 토큰입니다.")
        }

        // 2. 토큰에서 사용자 ID 추출
        val memberId = jwtTokenProvider.getMemberSeqFromToken(refreshToken)

        // 3. 새로운 토큰 발급
        return jwtTokenProvider.createTokens(memberId)
    }

    override fun withdrawal(member: Member) {
        if(member.withdrawalYn == "Y") throw InvalidRequestException("이미 탈퇴한 회원")

        val withdrawalService = oauthServiceFactory.getWithdrawalService(member.oauthProvider)
        withdrawalService.withdrawal(member)

        memberUseCase.withdrawal(member)
    }
}