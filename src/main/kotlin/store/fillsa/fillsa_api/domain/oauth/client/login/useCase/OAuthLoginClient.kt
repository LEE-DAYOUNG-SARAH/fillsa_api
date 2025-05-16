package store.fillsa.fillsa_api.domain.oauth.client.login.useCase

import org.springframework.util.LinkedMultiValueMap
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import java.time.LocalDateTime

interface OAuthLoginClient {
    /**
     *  access token 발급
     */
    fun getAccessToken(code: String): OAuthTokenInfo

    /**
     *  사용자 정보 조회
     */
    fun getUserInfo(accessToken: String): OAuthUserInfo

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
            "grant_type" to "authorization_code"
            "client_id" to clientId
            "client_secret" to clientSecret
            "redirect_uri" to redirectUri
            "code" to code
        }
    }
}

data class OAuthTokenInfo(
    val accessToken: String,
    val accessTokenExpiresAt: LocalDateTime,
    val refreshToken: String,
    val refreshTokenExpiresAt: LocalDateTime,
)

interface OAuthTokenResponse {
    fun toOAuthTokenInfo(): OAuthTokenInfo
}

data class OAuthUserInfo(
    val id: String,
    val nickname: String,
    val profileImageUrl: String?,
    val oAuthProvider: Member.OAuthProvider
)

interface OAuthUserResponse {
    fun toOAuthUserInfo(): OAuthUserInfo
}