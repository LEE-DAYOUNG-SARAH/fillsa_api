package store.fillsa.fillsa_api.domain.oauth.service.withdrawal

import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.client.withdrawl.useCase.KakaoOAuthWithdrawalClient
import store.fillsa.fillsa_api.domain.oauth.service.withdrawal.useCase.OAuthWithdrawalUseCase
import org.springframework.stereotype.Service

@Service
class KakaoOAuthWithdrawalService(
    private val kakaoOauthWithdrawalClient: KakaoOAuthWithdrawalClient
): OAuthWithdrawalUseCase {
    override fun withdraw(member: Member) {
        kakaoOauthWithdrawalClient.withdraw(member.oauthId)
    }

    override fun getOAuthProvider(): Member.OAuthProvider {
        return kakaoOauthWithdrawalClient.getOAuthProvider()
    }
}