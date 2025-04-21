package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import org.springframework.stereotype.Component

@Component
class OAuthServiceFactory(
    private val services: List<OAuthService>
) {
    fun getService(provider: Member.OAuthProvider): OAuthService {
        return services.find { it.provider == provider }
            ?: throw IllegalArgumentException("Unsupported OAuth provider: $provider")
    }
}