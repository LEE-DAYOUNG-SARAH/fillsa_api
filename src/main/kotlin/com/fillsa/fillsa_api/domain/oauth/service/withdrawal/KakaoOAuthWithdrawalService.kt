package com.fillsa.fillsa_api.domain.oauth.service.withdrawal

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.client.withdrawl.useCase.KakaoOAuthWithdrawalClient
import com.fillsa.fillsa_api.domain.oauth.service.withdrawal.useCase.OAuthWithdrawalUseCase
import org.springframework.stereotype.Service

@Service
class KakaoOAuthWithdrawalService(
    private val kakaoOauthWithdrawalClient: KakaoOAuthWithdrawalClient
): OAuthWithdrawalUseCase {
    override fun withdrawal(member: Member) {
        kakaoOauthWithdrawalClient.withdrawal(member.oauthId)
    }

    override fun getOAuthProvider(): Member.OAuthProvider {
        return kakaoOauthWithdrawalClient.getOAuthProvider()
    }
}