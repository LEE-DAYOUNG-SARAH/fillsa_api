package com.fillsa.fillsa_api.domain.oauth.service.callback

import com.fillsa.fillsa_api.common.exception.InvalidRequestException
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import com.fillsa.fillsa_api.domain.oauth.client.login.useCase.OAuthLoginClient
import com.fillsa.fillsa_api.domain.oauth.service.callback.useCase.OAuthCallbackUseCase
import com.fillsa.fillsa_api.domain.oauth.service.token.useCase.OAuthTokenUseCase
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class OAuthCallbackService(
    private val memberUseCase: MemberUseCase,
    private val oAuthTokenUseCase: OAuthTokenUseCase,
    private val clients: List<OAuthLoginClient>
): OAuthCallbackUseCase {
    val log = KotlinLogging.logger {  }

    override fun processOAuthCallback(provider: Member.OAuthProvider, code: String): Long {
        log .info { "$provider code: [$code]" }

        val client = clients.firstOrNull { it.getOAuthProvider() == provider }
            ?: throw InvalidRequestException("Unsupported provider: $provider")

        val oauthTokenInfo = client.getAccessToken(code)
        log.info { "$provider oauthTokenInfo: [$oauthTokenInfo]" }

        val userInfo = client.getUserInfo(oauthTokenInfo.accessToken)
        log.info { "$provider userInfo: [$userInfo]" }

        val member = memberUseCase.processOauthLogin(userInfo)

        oAuthTokenUseCase.createOAuthToken(member, oauthTokenInfo)

        return member.memberSeq
    }
}