package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import org.springframework.stereotype.Component

@Component
class OAuthCallbackServiceFactory(
    private val services: List<OAuthCallbackService>
) {
    fun getService(provider: Member.OAuthProvider): OAuthCallbackService{
        return services.find { it.provider == provider }
            ?: throw IllegalArgumentException("Unsupported OAuth provider: $provider")
    }
}