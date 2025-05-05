package store.fillsa.fillsa_api.domain.oauth.service.token

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.client.login.useCase.OAuthTokenInfo
import store.fillsa.fillsa_api.domain.oauth.entity.OAuthToken
import store.fillsa.fillsa_api.domain.oauth.respository.OAuthTokenRepository

@Service
class OAuthTokenService(
    private val oAuthTokenRepository: OAuthTokenRepository
) {

    @Transactional
    fun createOAuthToken(member: Member, oauthTokenInfo: OAuthTokenInfo): OAuthToken {
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