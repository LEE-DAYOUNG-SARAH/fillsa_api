package store.fillsa.fillsa_api.domain.oauth.service.callback

import mu.KotlinLogging
import org.springframework.stereotype.Service
import store.fillsa.fillsa_api.common.exception.ErrorCode.INVALID_REQUEST
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.service.MemberService
import store.fillsa.fillsa_api.domain.oauth.client.login.useCase.OAuthLoginClient
import store.fillsa.fillsa_api.domain.oauth.service.token.OAuthTokenService

@Service
class OAuthCallbackService(
    private val memberService: MemberService,
    private val oAuthTokenService: OAuthTokenService,
    private val clients: List<OAuthLoginClient>
) {
    val log = KotlinLogging.logger {  }

    fun processOAuthCallback(provider: Member.OAuthProvider, code: String): Long {
        log .info { "$provider code: [$code]" }

        val client = clients.firstOrNull { it.getOAuthProvider() == provider }
            ?: throw BusinessException(INVALID_REQUEST, "Unsupported provider: $provider")

        val oauthTokenInfo = client.getAccessToken(code)
        log.info { "$provider oauthTokenInfo: [$oauthTokenInfo]" }

        val userInfo = client.getUserInfo(oauthTokenInfo.accessToken)
        log.info { "$provider userInfo: [$userInfo]" }

        val member = memberService.processOauthLogin(userInfo)

        oAuthTokenService.createOAuthToken(member, oauthTokenInfo)

        return member.memberSeq
    }
}