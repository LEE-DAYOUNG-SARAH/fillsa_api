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
import com.fillsa.app.common.security.MemberAuthCache

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberDeviceService: MemberDeviceService,
    private val memberStreakService: MemberStreakService,
    private val memberAuthCache: MemberAuthCache,
) {
    @Transactional
    fun signUp(request: LoginRequest.LoginData): Member {
        val member = create(request.userData)
        memberDeviceService.create(member, request.deviceData)
        memberStreakService.create(member)
        // 재로그인 시 닉네임·프로필이 갱신될 수 있으므로 인증 캐시를 비운다
        memberAuthCache.evict(member.memberSeq)

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

    /**
     * 회원 탈퇴. 소프트 삭제(withdrawalYn='Y')이며 필사 기록은 보존한다.
     *
     * 디바이스를 함께 비활성화하지 않으면 탈퇴 후에도 푸시가 계속 발송된다.
     */
    @Transactional
    fun withdraw(member: Member) {
        val findMember = memberRepository.findByIdOrNull(member.memberSeq)
            ?: throw BusinessException(NOT_FOUND, "존재하지 않는 memberSeq: ${member.memberSeq}")

        if (findMember.isWithdrawal()) return  // 이미 탈퇴 — withdrawalAt 을 덮어쓰지 않는다

        findMember.withdrawal()
        memberDeviceService.withdrawAllDevices(findMember)
        // 캐시에 남아 있으면 탈퇴 후에도 최대 TTL 동안 토큰이 통과한다 → 커밋 직후까지 확실히 지운다
        memberAuthCache.evict(findMember.memberSeq)
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