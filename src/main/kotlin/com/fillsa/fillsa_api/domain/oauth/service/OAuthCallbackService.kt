package com.fillsa.fillsa_api.domain.oauth.service

import com.fillsa.fillsa_api.domain.auth.dto.LoginResponse
import com.fillsa.fillsa_api.domain.auth.security.JwtTokenProvider
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.MemberService
import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthLoginUseCase
import com.fillsa.fillsa_api.domain.oauth.service.useCase.OAuthCallbackUseCase
import mu.KotlinLogging

abstract class OAuthCallbackService(
    private val memberService: MemberService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val oAuthLoginClient: OAuthLoginUseCase
): OAuthCallbackUseCase {
    abstract val provider: Member.OAuthProvider

    val log = KotlinLogging.logger {  }

    override fun processOAuthCallback(code: String): LoginResponse {
        log .info { "$provider code: [$code]" }

        val accessToken = oAuthLoginClient.getAccessToken(code)
        log.info { "$provider accessToken: [$accessToken]" }

        val userInfo = oAuthLoginClient.getUserInfo(accessToken)
        log.info { "$provider userInfo: [$userInfo]" }

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