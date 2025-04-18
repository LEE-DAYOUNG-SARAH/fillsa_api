package com.fillsa.fillsa_api.domain.auth.service.useCase

import com.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.security.TokenInfo
import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface AuthUseCase {
    /**
     *  로그인
     */
    fun login(request: LoginRequest): LoginResponse

    /**
     *  액세스 토큰 받기
     */
    fun getAccessToken(code: String): String

    /**
     *  사용자 정보 받기
     */
    fun getAuthMember(accessToken: String): Member

    /**
     *  리프레시 토큰
     */
    fun refreshToken(refreshToken: String): TokenInfo
}