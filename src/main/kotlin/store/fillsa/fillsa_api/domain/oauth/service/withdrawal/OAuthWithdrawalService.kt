package store.fillsa.fillsa_api.domain.oauth.service.withdrawal

import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.oauth.service.withdrawal.useCase.OAuthWithdrawalUseCase
import org.springframework.stereotype.Service
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.INVALID_REQUEST

@Service
class OAuthWithdrawalService(
    private val withdrawalUseCases: List<OAuthWithdrawalUseCase>
) {
    fun withdraw(member: Member) {
        val useCase = withdrawalUseCases.firstOrNull { it.getOAuthProvider() == member.oauthProvider }
            ?: throw BusinessException(INVALID_REQUEST, "지원하지 않는 OAuth 제공자: ${member.oauthProvider} ")

        useCase.withdraw(member)
    }
}