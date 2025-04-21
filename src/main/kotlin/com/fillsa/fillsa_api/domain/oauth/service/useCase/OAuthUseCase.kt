package com.fillsa.fillsa_api.domain.oauth.service.useCase

import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse

interface OAuthUseCase {
    /**
     *  oauth 인증 응답
     */
    fun processOAuthCallback(code: String): LoginResponse
}