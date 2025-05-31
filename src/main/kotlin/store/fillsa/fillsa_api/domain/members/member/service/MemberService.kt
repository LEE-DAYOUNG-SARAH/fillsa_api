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
    private val memberDeviceService: MemberDeviceService
) {
    @Transactional
    fun signUp(request: LoginRequest.LoginData): Member {
        val member = create(request.userData)
        memberDeviceService.create(member, request.deviceData)

        return member
    }

    private fun create(request: LoginRequest.UserData): Member {
        val member = getActiveMemberByOauthId(request.oAuthId, request.oAuthProvider)

        return if(member == null) {
            memberRepository.save(request.toEntity())
        } else {
            member.update(request.nickname, request.profileImageUrl)
            member
        }
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

    private fun getActiveMemberByOauthId(id: String, provider: Member.OAuthProvider): Member? {
        return getAllMemberByOauthId(id, provider)
            .find { !it.isWithdrawal() }
    }

    @Transactional(readOnly = true)
    fun getAllMemberByOauthId(id: String, provider: Member.OAuthProvider): List<Member> {
        return memberRepository.findAllByOauthIdAndOauthProvider(id, provider)
    }
}