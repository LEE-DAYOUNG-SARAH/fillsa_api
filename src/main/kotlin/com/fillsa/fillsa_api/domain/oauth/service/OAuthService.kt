package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.oauth.client.OAuthClient
import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.oauth.service.useCase.OAuthUseCase
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.MemberService

abstract class OAuthService(
    private val memberService: MemberService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthClient: OAuthClient
): OAuthUseCase {
    abstract val provider: Member.OAuthProvider

    override fun processOAuthCallback(code: String): LoginResponse {
        val accessToken = oAuthClient.getAccessToken(code)
        val userInfo = oAuthClient.getUserInfo(accessToken)

        val member = memberService.processOauthLogin(userInfo)

        val token = jwtTokenProvider.createTokens(member.memberSeq)

        return LoginResponse(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            memberSeq = member.memberSeq,
            nickname = member.nickname.orEmpty()
        )
    }
}