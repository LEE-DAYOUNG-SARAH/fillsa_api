package store.fillsa.fillsa_api.domain.members.member.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode.NOT_FOUND
import store.fillsa.fillsa_api.common.exception.ErrorCode.WITHDRAWAL_USER
import store.fillsa.fillsa_api.domain.auth.dto.LoginRequest
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.member.repository.MemberRepository

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {
    @Transactional
    fun signUp(provider: Member.OAuthProvider, loginData: LoginRequest.LoginData): Member {
        return memberRepository.findByOauthIdAndOauthProvider(loginData.id, provider)
            ?: createMember(provider, loginData)
    }

    private fun createMember(provider: Member.OAuthProvider, loginData: LoginRequest.LoginData): Member {
        return memberRepository.save(
            Member(
                oauthId = loginData.id,
                oauthProvider = provider,
                nickname = loginData.nickname,
                profileImageUrl = loginData.profileImageUrl
            )
        )
    }

    @Transactional
    fun withdraw(member: Member) {
        val findMember = memberRepository.findByIdOrNull(member.memberSeq)
        findMember?.withdrawal()
    }

    @Transactional(readOnly = true)
    fun getActiveMemberBySeq(seq: Long): Member {
        val member = memberRepository.findByIdOrNull(seq) ?: throw BusinessException(NOT_FOUND, "존재하지 않는 memberSeq")
        if(member.isWithdrawal()) throw BusinessException(WITHDRAWAL_USER)

        return member
    }
}