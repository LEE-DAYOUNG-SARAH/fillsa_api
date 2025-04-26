package com.fillsa.fillsa_api.domain.oauth.service.token

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.client.login.useCase.OAuthTokenInfo
import com.fillsa.fillsa_api.domain.oauth.entity.OAuthToken
import com.fillsa.fillsa_api.domain.oauth.respository.OAuthTokenRepository
import com.fillsa.fillsa_api.domain.oauth.service.token.useCase.OAuthTokenUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OAuthTokenService(
    private val oAuthTokenRepository: OAuthTokenRepository
): OAuthTokenUseCase {

    @Transactional
    override fun createOAuthToken(member: Member, oauthTokenInfo: OAuthTokenInfo): OAuthToken {
        return oAuthTokenRepository.save(
            OAuthToken(
                member = member,
                accessToken = oauthTokenInfo.accessToken,
                accessTokenExpiresAt = oauthTokenInfo.accessTokenExpiresAt,
                refreshToken = oauthTokenInfo.refreshToken,
                refreshTokenExpiresAt = oauthTokenInfo.refreshTokenExpiresAt
            )
        )
    }
}