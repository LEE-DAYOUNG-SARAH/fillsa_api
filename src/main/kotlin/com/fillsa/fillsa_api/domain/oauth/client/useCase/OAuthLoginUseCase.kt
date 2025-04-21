package com.fillsa.fillsa_api.domain.oauth.client.useCase

import com.fillsa.fillsa_api.domain.members.member.entity.Member

interface OAuthLoginService {
    fun getAccessToken(code: String): String
    fun getUserInfo(accessToken: String): OAuthUserInfo
}

data class OAuthUserInfo(
    val id: String,
    val nickname: String,
    val profileImageUrl: String?,
    val oAuthProvider: Member.OAuthProvider
)