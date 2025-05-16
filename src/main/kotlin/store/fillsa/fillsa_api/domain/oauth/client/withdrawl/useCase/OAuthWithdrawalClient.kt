package store.fillsa.fillsa_api.domain.oauth.client.withdrawl.useCase

import org.springframework.util.LinkedMultiValueMap
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

    data class OAuthTokenRequest(
        val clientId: String,
        val clientSecret: String,
        val refreshToken: String
    ) {
        fun toMultiValueMap() = LinkedMultiValueMap<String, String>().apply {
            "grant_type" to "refresh_token"
            "client_id" to clientId
            "client_secret" to clientSecret
            "refresh_token" to refreshToken
        }
    }
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