package com.fillsa.fillsa_api.domain.oauth.client

import com.fasterxml.jackson.databind.JsonNode
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthUserInfo
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KakaoOAuthClient(
    webClient: WebClient,
    @Value("\${oauth.kakao.client-id}") clientId: String,
    @Value("\${oauth.kakao.client-secret}") clientSecret: String,
    @Value("\${oauth.kakao.redirect-uri}") redirectUri: String,

    @Value("\${oauth.kakao.token-uri}") tokenUri: String,
    @Value("\${oauth.kakao.user-info-uri}") userInfoUri: String,
): OAuthClient(webClient, clientId, clientSecret, redirectUri, tokenUri, userInfoUri) {

    override val provider = Member.OAuthProvider.KAKAO

    override fun parseUserInfo(raw: JsonNode): OAuthUserInfo {
        val kakaoAccount = raw["kakao_account"]
        return OAuthUserInfo(
            id = raw["id"].asText(),
            nickname = kakaoAccount["profile"]?.get("nickname")?.asText().orEmpty(),
            profileImageUrl = kakaoAccount["profile"]?.get("profileImage")?.asText(),
            oAuthProvider = provider
        )
    }
}