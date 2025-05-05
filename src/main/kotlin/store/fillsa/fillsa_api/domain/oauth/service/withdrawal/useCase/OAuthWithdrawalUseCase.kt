package store.fillsa.fillsa_api.domain.oauth.service.withdrawal.useCase

import store.fillsa.fillsa_api.domain.members.member.entity.Member

interface OAuthWithdrawalUseCase {
    /**
     *  탈퇴
     */
    fun withdraw(member: Member)

    /**
     * oauth provider 조회
     */
    fun getOAuthProvider(): Member.OAuthProvider
}