package store.fillsa.fillsa_api.domain.oauth.client.token.useCase

import org.springframework.util.LinkedMultiValueMap
import store.fillsa.fillsa_api.domain.members.member.entity.Member

interface OAuthTokenClient {
    /**
     *  access token 발급
     */
    fun getAccessToken(code: String): String

    /**
     *  OAuth 공급자 반환
     */
    fun getOAuthProvider(): Member.OAuthProvider

    data class OAuthTokenRequest(
        val clientId: String,
        val clientSecret: String,
        val redirectUri: String,
        val code: String
    ) {
        fun toMultiValueMap() = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", redirectUri)
            add("code", code)
        }
    }
}