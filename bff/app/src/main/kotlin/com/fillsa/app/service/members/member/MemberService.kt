package com.fillsa.app.service.members.member

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode.NOT_FOUND
import com.fillsa.util.exception.ErrorCode.WITHDRAWAL_USER
import com.fillsa.app.api.auth.LoginRequest
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberRepository
import com.fillsa.app.service.members.quote.MemberStreakService

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberDeviceService: MemberDeviceService,
    private val memberStreakService: MemberStreakService
) {
    @Transactional
    fun signUp(request: LoginRequest.LoginData): Member {
        val member = create(request.userData)
        memberDeviceService.create(member, request.deviceData)
        memberStreakService.create(member)

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