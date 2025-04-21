package com.fillsa.fillsa_api.domain.auth.service.useCase

import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse

interface OauthUseCase {
    /**
     *  oauth 인증 응답
     */
    fun processOauthCallback(code: String): LoginResponse
}