package com.fillsa.fillsa_api.domain.auth.service.useCase

import com.fillsa.fillsa_api.domain.auth.security.TokenInfo

interface AuthUseCase {
    /**
     *  리프레시 토큰
     */
    fun refreshToken(refreshToken: String): TokenInfo
}