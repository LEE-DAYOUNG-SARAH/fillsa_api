package store.fillsa.fillsa_api.domain.oauth.client.user.useCase

import store.fillsa.fillsa_api.domain.members.member.entity.Member

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