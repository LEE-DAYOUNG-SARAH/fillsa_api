package com.fillsa.fillsa_api.domain.members.member.service

import com.fillsa.fillsa_api.domain.auth.client.KakaoUserInfo
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
    override fun processOauthLogin(
        oauthId: String,
        nickname: String,
        profileImageUrl: String?,
        oauthProvider: Member.OauthProvider
    ): Member {
        return memberRepository.findByOauthIdAndOauthProvider(oauthId, oauthProvider)
            ?: createMember(oauthId, nickname, profileImageUrl, oauthProvider)
    }

    private fun createMember(
        oauthId: String,
        nickname: String,
        profileImageUrl: String?,
        oauthProvider: Member.OauthProvider
    ): Member {
        return memberRepository.save(
            Member.createOAuthMember(
                oauthId = oauthId,
                oauthProvider = oauthProvider,
                nickname = nickname,
                profileImageUrl = profileImageUrl
            )
        )
    }
}