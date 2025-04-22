package com.fillsa.fillsa_api.domain.oauth.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fillsa.fillsa_api.common.exception.OAuthLoginException
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthLoginUseCase
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthUserInfo
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono


abstract class OAuthLoginClient(
    private val webClient: WebClient,
    private val clientId: String,
    private val clientSecret: String,
    private val redirectUri: String,
    private val tokenUri: String,
    private val userInfoUri: String,
): OAuthLoginUseCase {
    protected abstract val provider: Member.OAuthProvider
    override fun getAccessToken(code: String): String {
        val request = OAuthTokenRequest(
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUri = redirectUri,
            code = code
        )

        return webClient.post()
            .uri(tokenUri)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<OAuthTokenResponse>()
            .map { it.accessToken }
            .block()
            ?: throw OAuthLoginException("$provider 액세스 토큰을 받아오는데 실패했습니다.")
    }

    override fun getUserInfo(accessToken: String): OAuthUserInfo {
        val headers = HttpHeaders().apply {
            setBearerAuth(accessToken)
        }

        val raw = webClient.get()
            .uri(userInfoUri)
            .headers { it.addAll(headers) }
            .retrieve()
            .bodyToMono<JsonNode>()
            .block()
            ?: throw OAuthLoginException("$provider 사용자 정보를 받아오는데 실패했습니다.")

        return parseUserInfo(raw)
    }

    protected abstract fun parseUserInfo(json: JsonNode): OAuthUserInfo

    data class OAuthTokenRequest(
        @JsonProperty("grant_type")
        val grantType: String = "authorization_code",

        @JsonProperty("client_id")
        val clientId: String,

        @JsonProperty("client_secret")
        val clientSecret: String,

        @JsonProperty("redirect_uri")
        val redirectUri: String,

        @JsonProperty("code")
        val code: String
    )

    data class OAuthTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,

        @JsonProperty("token_type")
        val tokenType: String,

        @JsonProperty("refresh_token")
        val refreshToken: String,

        @JsonProperty("expires_in")
        val expiresIn: Int,

        @JsonProperty("scope")
        val scope: String,

        @JsonProperty("refresh_token_expires_in")
        val refreshTokenExpiresIn: Int
    )
}

