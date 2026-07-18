package com.fillsa.app.api.oauth.client.user.useCase

import com.fillsa.service.member.Member

interface OAuthUserClient {
    /**
     *  oauth user 조회
     */
    fun getOAuthId(accessToken: String): String

    /**
     *  OAuth 공급자 반환
     */
    fun getOAuthProvider(): Member.OAuthProvider
}