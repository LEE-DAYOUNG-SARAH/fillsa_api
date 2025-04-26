package com.fillsa.fillsa_api.domain.auth.service.useCase

import com.fillsa.fillsa_api.domain.auth.dto.TokenRefreshRequest
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface AuthUseCase {
    /**
     *  리프레시 토큰
     */
    fun refreshToken(request: TokenRefreshRequest): TokenInfo

    /**
     *  탈퇴
     */
    fun withdrawal(member: Member)
}