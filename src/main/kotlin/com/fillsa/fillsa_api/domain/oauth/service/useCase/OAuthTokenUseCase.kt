package com.fillsa.fillsa_api.domain.oauth.service.useCase

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthTokenInfo
import com.fillsa.fillsa_api.domain.oauth.entity.OAuthToken

interface OAuthTokenUseCase {
    /**
     *  토큰 저장
     */
    fun createOAuthToken(member: Member, oauthTokenInfo: OAuthTokenInfo): OAuthToken
}