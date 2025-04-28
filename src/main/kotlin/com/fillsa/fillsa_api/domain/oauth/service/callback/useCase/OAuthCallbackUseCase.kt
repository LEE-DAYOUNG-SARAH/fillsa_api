package com.fillsa.fillsa_api.domain.oauth.service.callback.useCase

import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface OAuthCallbackUseCase {
    /**
     *  oauth 인증 응답
     */
    fun processOAuthCallback(provider: Member.OAuthProvider, code: String): Long
}