package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.MemberService
import com.fillsa.fillsa_api.domain.oauth.client.OAuthLoginClient
import com.fillsa.fillsa_api.domain.oauth.service.useCase.OAuthCallbackUseCase

abstract class OAuthCallbackService(
    private val memberService: MemberService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthLoginClient: OAuthLoginClient
): OAuthCallbackUseCase {
    abstract val provider: Member.OAuthProvider

    override fun processOAuthCallback(code: String): LoginResponse {
        val accessToken = oAuthLoginClient.getAccessToken(code)
        val userInfo = oAuthLoginClient.getUserInfo(accessToken)

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