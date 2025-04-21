package com.fillsa.fillsa_api.domain.oauth.client

import com.fasterxml.jackson.databind.JsonNode
import com.fillsa.fillsa_api.common.exception.OAuthLoginException
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthLoginService
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthUserInfo
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

abstract class OAuthClient(
    private val webClient: WebClient,

    private val clientId: String,
    private val clientSecret: String,
    private val redirectUri: String,

    private val tokenUri: String,
    private val userInfoUri: String,
): OAuthLoginService {
    protected abstract val provider: Member.OAuthProvider
    override fun getAccessToken(code: String): String {
        val request = mapOf(
            "grant_type" to "authorization_code",
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "redirect_uri" to redirectUri,
            "code" to code
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
            .bodyToMono(JsonNode::class.java)
            .block()
            ?: throw OAuthLoginException("$provider 사용자 정보를 받아오는데 실패했습니다.")

        return parseUserInfo(raw)
    }

    protected abstract fun parseUserInfo(raw: JsonNode): OAuthUserInfo

    data class OAuthTokenResponse(
        val accessToken: String,
        val tokenType: String,
        val expiresIn: Int,
        val scope: String?
    )
}

