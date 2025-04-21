package com.fillsa.fillsa_api.domain.auth.service

import com.fillsa.fillsa_api.common.exception.InvalidRequestException
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.auth.service.useCase.AuthUseCase
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val jwtTokenProvider: JwtTokenProvider
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
}