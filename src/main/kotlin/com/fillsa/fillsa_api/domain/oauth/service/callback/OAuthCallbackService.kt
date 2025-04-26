package com.fillsa.fillsa_api.domain.oauth.service.callback

import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import com.fillsa.fillsa_api.domain.oauth.client.login.useCase.OAuthLoginClient
import com.fillsa.fillsa_api.domain.oauth.service.callback.useCase.OAuthCallbackUseCase
import com.fillsa.fillsa_api.domain.oauth.service.token.useCase.OAuthTokenUseCase
import mu.KotlinLogging

abstract class OAuthCallbackService(
    private val memberUseCase: MemberUseCase,
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthLoginClient: OAuthLoginClient,
    private val oAuthTokenUseCase: OAuthTokenUseCase
): OAuthCallbackUseCase {
    val log = KotlinLogging.logger {  }

    override fun processOAuthCallback(code: String): LoginResponse {
        log .info { "${getOAuthProvider()} code: [$code]" }

        val oauthTokenInfo = oAuthLoginClient.getAccessToken(code)
        log.info { "${getOAuthProvider()} oauthTokenInfo: [$oauthTokenInfo]" }

        val userInfo = oAuthLoginClient.getUserInfo(oauthTokenInfo.accessToken)
        log.info { "${getOAuthProvider()} userInfo: [$userInfo]" }

        val member = memberUseCase.processOauthLogin(userInfo)

        oAuthTokenUseCase.createOAuthToken(member, oauthTokenInfo)

        val token = jwtTokenProvider.createTokens(member.memberSeq)

        return LoginResponse(
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            memberSeq = member.memberSeq,
            nickname = member.nickname.orEmpty()
        )
    }

    override fun getOAuthProvider(): Member.OAuthProvider {
        return oAuthLoginClient.getOAuthProvider()
    }
}