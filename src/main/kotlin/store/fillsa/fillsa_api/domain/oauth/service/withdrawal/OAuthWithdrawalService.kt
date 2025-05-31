package store.fillsa.fillsa_api.domain.oauth.service.withdrawal

import mu.KotlinLogging
import org.springframework.stereotype.Service
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.INVALID_REQUEST
import store.fillsa.fillsa_api.domain.auth.service.auth.AuthService
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.client.token.useCase.OAuthTokenClient
import store.fillsa.fillsa_api.domain.oauth.client.user.useCase.OAuthUserClient

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