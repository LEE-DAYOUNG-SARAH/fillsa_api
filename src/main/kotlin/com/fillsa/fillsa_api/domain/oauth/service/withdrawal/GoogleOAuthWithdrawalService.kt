package com.fillsa.fillsa_api.domain.oauth.service.withdrawal

import com.fillsa.fillsa_api.common.exception.OAuthWithdrawalException
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.client.withdrawl.useCase.GoogleOAuthWithdrawalClient
import com.fillsa.fillsa_api.domain.oauth.respository.OAuthTokenRepository
import com.fillsa.fillsa_api.domain.oauth.service.withdrawal.useCase.OAuthWithdrawalUseCase
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GoogleOAuthWithdrawalService(
    private val googleOAuthWithdrawalClient: GoogleOAuthWithdrawalClient,
    private val oAuthTokenRepository: OAuthTokenRepository
): OAuthWithdrawalUseCase {

    override fun withdraw(member: Member) {
        val oauthToken = oAuthTokenRepository.findTopByMemberOrderByOauthTokenSeqDesc(member)
            ?: throw OAuthWithdrawalException("OAuth 정보 없음")

        val refreshToken = if(oauthToken.refreshTokenExpiresAt > LocalDateTime.now()) {
            oauthToken.refreshToken
        } else throw OAuthWithdrawalException("OAuth refresh token 만료")

        val accessToken = googleOAuthWithdrawalClient.getAccessToken(refreshToken)

        googleOAuthWithdrawalClient.withdraw(accessToken)
    }

    override fun getOAuthProvider(): Member.OAuthProvider {
        return googleOAuthWithdrawalClient.getOAuthProvider()
    }
}