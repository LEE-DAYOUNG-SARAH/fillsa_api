package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthLoginUseCase
import com.fillsa.fillsa_api.domain.oauth.service.useCase.OAuthCallbackUseCase
import com.fillsa.fillsa_api.domain.oauth.service.useCase.OAuthTokenUseCase
import mu.KotlinLogging

abstract class OAuthCallbackService(
    private val memberUseCase: MemberUseCase,
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthLoginClient: OAuthLoginUseCase,
    private val oAuthTokenUseCase: OAuthTokenUseCase
): OAuthCallbackUseCase {
    abstract val provider: Member.OAuthProvider

    val log = KotlinLogging.logger {  }

    override fun processOAuthCallback(code: String): LoginResponse {
        log .info { "$provider code: [$code]" }

        val oauthTokenInfo = oAuthLoginClient.getAccessToken(code)
        log.info { "$provider oauthTokenInfo: [$oauthTokenInfo]" }

        val userInfo = oAuthLoginClient.getUserInfo(oauthTokenInfo.accessToken)
        log.info { "$provider userInfo: [$userInfo]" }

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
}