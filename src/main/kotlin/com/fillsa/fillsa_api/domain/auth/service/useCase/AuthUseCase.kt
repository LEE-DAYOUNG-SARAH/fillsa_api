package com.fillsa.fillsa_api.domain.auth.service.useCase

import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface AuthUseCase {
    /**
     *  리프레시 토큰
     */
    fun refreshToken(refreshToken: String): TokenInfo

    /**
     *  탈퇴
     */
    fun withdrawal(member: Member)
}