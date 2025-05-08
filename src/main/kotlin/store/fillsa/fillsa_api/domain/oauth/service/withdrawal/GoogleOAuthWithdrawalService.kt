package store.fillsa.fillsa_api.domain.oauth.service.withdrawal

import org.springframework.stereotype.Service
import store.fillsa.fillsa_api.common.exception.ErrorCode.NOT_FOUND
import store.fillsa.fillsa_api.common.exception.ErrorCode.OAUTH_REFRESH_TOKEN_EXPIRED
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.client.withdrawl.useCase.GoogleOAuthWithdrawalClient
import store.fillsa.fillsa_api.domain.oauth.respository.OAuthTokenRepository
import store.fillsa.fillsa_api.domain.oauth.service.withdrawal.useCase.OAuthWithdrawalUseCase
import java.time.LocalDateTime

@Service
class GoogleOAuthWithdrawalService(
    private val googleOAuthWithdrawalClient: GoogleOAuthWithdrawalClient,
    private val oAuthTokenRepository: OAuthTokenRepository
): OAuthWithdrawalUseCase {

    override fun withdraw(member: Member) {
        val oauthToken = oAuthTokenRepository.findTopByMemberOrderByOauthTokenSeqDesc(member)
            ?: throw BusinessException(NOT_FOUND, "OAuth 정보 없음")

        val refreshToken = if(oauthToken.refreshTokenExpiresAt > LocalDateTime.now()) {
            oauthToken.refreshToken
        } else throw BusinessException(OAUTH_REFRESH_TOKEN_EXPIRED)

        val accessToken = googleOAuthWithdrawalClient.getAccessToken(refreshToken)

        googleOAuthWithdrawalClient.withdraw(accessToken)
    }

    override fun getOAuthProvider(): Member.OAuthProvider {
        return googleOAuthWithdrawalClient.getOAuthProvider()
    }
}