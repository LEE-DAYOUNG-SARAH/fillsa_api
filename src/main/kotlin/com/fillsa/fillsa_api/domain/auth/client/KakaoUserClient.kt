package com.fillsa.fillsa_api.domain.auth.client

import com.fillsa.fillsa_api.common.exception.OAuthLoginException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KakaoUserClient(
    private val webClient: WebClient,

    @Value("\${security.oauth2.client.provider.kakao.user-info-uri}")
    private var userInfoUri: String,
) {

    fun getUserInfo(accessToken: String): KakaoUserInfo {
        return webClient.get()
            .uri(userInfoUri)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono<KakaoUserInfo>()
            .block()
            ?: throw OAuthLoginException("카카오 사용자 정보를 받아오는데 실패했습니다.")
    }
}

data class KakaoUserInfo(
    val id: Long,
    val kakaoAccount: KakaoAccount,
    val properties: Properties
) {
    fun getNickname() = properties.nickname
    fun getProfileImage() = properties.profileImage

    data class KakaoAccount(
        val email: String
    )

    data class Properties(
        val nickname: String,
        val profileImage: String?
    )
}