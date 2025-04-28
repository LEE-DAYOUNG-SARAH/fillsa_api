package com.fillsa.fillsa_api.domain.members.member.service

import com.fillsa.fillsa_api.common.exception.InvalidRequestException
import com.fillsa.fillsa_api.common.exception.NotFoundException
import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.member.repository.MemberRepository
import com.fillsa.fillsa_api.domain.members.member.service.useCase.MemberUseCase
import com.fillsa.fillsa_api.domain.oauth.client.login.useCase.OAuthUserInfo
import org.springframework.data.repository.findByIdOrNull
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
            Member(
                oauthId = oAuthUserInfo.id,
                oauthProvider = oAuthUserInfo.oAuthProvider,
                nickname = oAuthUserInfo.nickname,
                profileImageUrl = oAuthUserInfo.profileImageUrl
            )
        )
    }

    @Transactional
    override fun withdraw(member: Member) {
        val findMember = memberRepository.getReferenceById(member.memberSeq)
        findMember.withdrawal()
    }

    @Transactional(readOnly = true)
    override fun getActiveMemberBySeq(seq: Long): Member {
        val member = memberRepository.findByIdOrNull(seq) ?: throw NotFoundException("존재하지 않는 회원")
        if(member.isWithdrawal()) throw InvalidRequestException("탈퇴한 회원")

        return member
    }
}