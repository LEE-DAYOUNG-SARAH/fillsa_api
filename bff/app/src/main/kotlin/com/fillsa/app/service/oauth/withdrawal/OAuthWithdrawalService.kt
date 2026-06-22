package com.fillsa.app.service.oauth.withdrawal

import mu.KotlinLogging
import org.springframework.stereotype.Service
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode.INVALID_REQUEST
import com.fillsa.app.service.auth.AuthService
import com.fillsa.service.member.Member
import com.fillsa.app.api.oauth.client.token.useCase.OAuthTokenClient
import com.fillsa.app.api.oauth.client.user.useCase.OAuthUserClient

@Service
class OAuthWithdrawalService(
    private val oAuthTokenClients: List<OAuthTokenClient>,
    private val oAuthUserClients: List<OAuthUserClient>,
    private val authService: AuthService
) {
    val log = KotlinLogging.logger {  }

    fun withdraw(provider: Member.OAuthProvider, code: String) {
        val accessToken = oAuthTokenClients.find { it.getOAuthProvider() == provider }
            ?.getAccessToken(code)
            ?: throw BusinessException(INVALID_REQUEST)
        log.debug { "간편 로그인 콜백 accessToken: [$accessToken]" }

        val oAuthId = oAuthUserClients.find { it.getOAuthProvider() == provider }
            ?.getOAuthId(accessToken)
            ?: throw BusinessException(INVALID_REQUEST)
        log.debug { "간편 로그인 콜백 oAuthId: [$oAuthId]" }

        authService.withdrawByWeb(oAuthId, provider)
    }
}