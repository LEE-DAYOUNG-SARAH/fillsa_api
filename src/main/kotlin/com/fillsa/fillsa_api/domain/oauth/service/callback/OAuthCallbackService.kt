package com.fillsa.fillsa_api.domain.oauth.service.callback

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import com.fillsa.fillsa_api.domain.oauth.client.login.useCase.OAuthLoginClient
import com.fillsa.fillsa_api.domain.oauth.service.callback.useCase.OAuthCallbackUseCase
import mu.KotlinLogging

abstract class OAuthCallbackService(
    private val memberUseCase: MemberUseCase,
    private val oAuthLoginClient: OAuthLoginClient,
): OAuthCallbackUseCase {
    val log = KotlinLogging.logger {  }

    override fun processOAuthCallback(code: String): Long {
        log .info { "${getOAuthProvider()} code: [$code]" }

        val oauthTokenInfo = oAuthLoginClient.getAccessToken(code)
        log.info { "${getOAuthProvider()} oauthTokenInfo: [$oauthTokenInfo]" }

        val userInfo = oAuthLoginClient.getUserInfo(oauthTokenInfo.accessToken)
        log.info { "${getOAuthProvider()} userInfo: [$userInfo]" }

        val member = memberUseCase.processOauthLogin(userInfo)
        return member.memberSeq
    }

    override fun getOAuthProvider(): Member.OAuthProvider {
        return oAuthLoginClient.getOAuthProvider()
    }
}