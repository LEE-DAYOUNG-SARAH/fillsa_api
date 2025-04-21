package com.fillsa.fillsa_api.domain.members.member.service

import com.fillsa.fillsa_api.domain.oauth.client.useCase.OAuthUserInfo
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
): MemberUseCase {

    @Transactional
    override fun processOauthLogin(oAuthUserInfo: OAuthUserInfo): Member {
        return memberRepository.findByOauthIdAndOauthProvider(oAuthUserInfo.id, oAuthUserInfo.oAuthProvider)
            ?: createMember(oAuthUserInfo)
    }

    private fun createMember(oAuthUserInfo: OAuthUserInfo): Member {
        return memberRepository.save(
            Member.createOAuthMember(
                oauthId = oAuthUserInfo.id,
                oauthProvider = oAuthUserInfo.oAuthProvider,
                nickname = oAuthUserInfo.nickname,
                profileImageUrl = oAuthUserInfo.profileImageUrl
            )
        )
    }
}