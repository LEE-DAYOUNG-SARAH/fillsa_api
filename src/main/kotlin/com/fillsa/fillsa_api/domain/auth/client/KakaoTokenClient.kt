package com.fillsa.fillsa_api.domain.auth.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fillsa.fillsa_api.common.exception.OAuthLoginException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KakaoTokenClient(
    private val webClient: WebClient,

    @Value("\${security.oauth2.client.registration.kakao.client-id}")
    private val clientId: String,
    @Value("\${security.oauth2.client.registration.kakao.client-secret}")
    private val clientSecret: String,
    @Value("\${security.oauth2.client.registration.kakao.redirect-uri}")
    private val redirectUri: String,
    @Value("\${security.oauth2.client.registration.kakao.authorization-grant-type}")
    private val grantType: String,


    @Value("\${security.oauth2.client.provider.kakao.token-uri}")
    private var tokenUri: String,
) {

    fun getAccessToken(code: String): String {
        val request = KakaoTokenRequest(
            grantType = grantType,
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUri = redirectUri,
            code = code
        )

        return webClient.post()
            .uri(tokenUri)
            .bodyValue(request)
            .retrieve()
            .bodyToMono<KakaoTokenResponse>()
            .map { it.accessToken }
            .block()
            ?: throw OAuthLoginException("카카오 액세스 토큰을 받아오는데 실패했습니다.")
    }
}

data class KakaoTokenRequest(
    @JsonProperty("grant_type")
    val grantType: String,
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
    @JsonProperty("redirect_uri")
    val redirectUri: String,
    val code: String
)

data class KakaoTokenResponse(
    val accessToken: String,
    val tokenType: String,
    val refreshToken: String,
    val expiresIn: Int,
    val scope: String?,
    val refreshTokenExpiresIn: Int
)