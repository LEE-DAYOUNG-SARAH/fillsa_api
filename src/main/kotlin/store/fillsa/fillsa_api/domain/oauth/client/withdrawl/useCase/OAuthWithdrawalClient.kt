package store.fillsa.fillsa_api.domain.oauth.client.withdrawl.useCase

import store.fillsa.fillsa_api.domain.members.member.entity.Member

interface GoogleOAuthWithdrawalClient {
    /**
     *  토큰 발급
     */
    fun getAccessToken(refreshToken: String): String

    /**
     *  탈퇴
     */
    fun withdraw(accessToken: String)

    /**
     *  OAuth 공급자 반환
     */
    fun getOAuthProvider(): Member.OAuthProvider
}

interface KakaoOAuthWithdrawalClient {
    /**
     *  탈퇴
     */
    fun withdraw(oauthId: String)

    /**
     *  OAuth 공급자 반환
     */
    fun getOAuthProvider(): Member.OAuthProvider
}