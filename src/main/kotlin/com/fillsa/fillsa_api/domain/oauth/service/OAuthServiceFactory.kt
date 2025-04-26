package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.service.callback.OAuthCallbackService
import com.fillsa.fillsa_api.domain.oauth.service.withdrawal.useCase.OAuthWithdrawalUseCase
import org.springframework.stereotype.Component

@Component
class OAuthServiceFactory(
    private val callbackServices: List<OAuthCallbackService>,
    private val withdrawalServices: List<OAuthWithdrawalUseCase>
) {
    fun getCallbackService(provider: Member.OAuthProvider): OAuthCallbackService {
        return callbackServices.find { it.getOAuthProvider() == provider }
            ?: throw IllegalArgumentException("Unsupported OAuth provider: $provider")
    }

    fun getWithdrawalService(provider: Member.OAuthProvider): OAuthWithdrawalUseCase {
        return withdrawalServices.find { it.getOAuthProvider() == provider }
            ?: throw IllegalArgumentException("Unsupported OAuth provider: $provider")
    }
}