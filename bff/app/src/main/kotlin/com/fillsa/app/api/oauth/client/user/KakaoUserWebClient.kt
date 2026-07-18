package com.fillsa.app.api.oauth.client.user

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import com.fillsa.service.member.Member

@Component
class KakaoUserWebClient(
    webClient: WebClient,
    @Value("\${oauth.kakao.user-info-uri}") userInfoUri: String,
): OAuthUserWebClient(webClient, userInfoUri) {
    override fun getOAuthProvider() = Member.OAuthProvider.KAKAO
}