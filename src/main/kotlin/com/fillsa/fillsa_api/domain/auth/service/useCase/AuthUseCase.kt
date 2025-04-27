package com.fillsa.fillsa_api.domain.auth.service.useCase

import com.fillsa.fillsa_api.domain.auth.dto.*
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface AuthUseCase {
    /**
     *  리프레시 토큰 발급
     */
    fun refreshToken(request: TokenRefreshRequest): TokenInfo

    /**
     *  탈퇴
     */
    fun withdrawal(member: Member, request: WithdrawalRequest)

    /**
     *  로그아웃
     */
    fun logout(member: Member, request: LogoutRequest)

    /**
     *  로그인
     */
    fun login(request: TempTokenRequest): LoginResponse
}