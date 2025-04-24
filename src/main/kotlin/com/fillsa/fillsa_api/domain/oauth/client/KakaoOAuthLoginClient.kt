package com.fillsa.fillsa_api.domain.oauth.client

import com.fasterxml.jackson.databind.JsonNode
import com.fillsa.fillsa_api.common.exception.OAuthLoginException
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthUserInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class KakaoOAuthLoginClient(
    webClient: WebClient,
    @Value("\${oauth.kakao.client-id}") clientId: String,
    @Value("\${oauth.kakao.client-secret}") clientSecret: String,
    @Value("\${oauth.kakao.redirect-uri}") redirectUri: String,

    @Value("\${oauth.kakao.token-uri}") tokenUri: String,
    @Value("\${oauth.kakao.user-info-uri}") userInfoUri: String,
): OAuthLoginClient(
    webClient, clientId, clientSecret, redirectUri, tokenUri, userInfoUri
) {
    override val provider = Member.OAuthProvider.KAKAO

    override fun parseUserInfo(json: JsonNode?): OAuthUserInfo {
        requireNotNull(json) { "$provider 사용자 정보가 없습니다." }
        val profile = json["kakao_account"]?.get("profile")
            ?: throw OAuthLoginException("$provider 프로필 정보가 없습니다.")

        return OAuthUserInfo(
            id = json["id"].asText(),
            nickname = profile["nickname"].asText(),
            profileImageUrl = profile["profile_image_url"]?.asText(),
            oAuthProvider = provider
        )
    }
}