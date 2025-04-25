package com.fillsa.fillsa_api.domain.oauth.client.useCase

import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface OAuthLoginUseCase {
    /**
     *  access token 발급
     */
    fun getAccessToken(code: String): String

    /**
     *  사용자 정보 조회
     */
    fun getUserInfo(accessToken: String): OAuthUserInfo

    /**
     *  OAuth 공급자 반환
     */
    fun getOAuthProvider(): Member.OAuthProvider
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