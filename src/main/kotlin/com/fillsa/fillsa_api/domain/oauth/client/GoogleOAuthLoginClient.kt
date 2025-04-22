package com.fillsa.fillsa_api.domain.oauth.client

import com.fasterxml.jackson.databind.JsonNode
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthUserInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class GoogleOAuthLoginClient(
    webClient: WebClient,
    @Value("\${oauth.google.client-id}") clientId: String,
    @Value("\${oauth.google.client-secret}") clientSecret: String,
    @Value("\${oauth.google.redirect-uri}") redirectUri: String,
    @Value("\${oauth.google.token-uri}") tokenUri: String,
    @Value("\${oauth.google.user-info-uri}") userInfoUri: String,
) : OAuthLoginClient(
    webClient, clientId, clientSecret, redirectUri, tokenUri, userInfoUri
) {
    override val provider = Member.OAuthProvider.GOOGLE

    override fun parseUserInfo(json: JsonNode) = OAuthUserInfo(
        id = json["id"].asText(),
        nickname = json["name"].asText(),
        profileImageUrl = json["picture"].asText(),
        oAuthProvider = provider
    )
}