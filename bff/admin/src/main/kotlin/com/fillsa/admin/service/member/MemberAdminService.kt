package com.fillsa.admin.service.member

import com.fillsa.admin.common.dto.PageEnvelope
import com.fillsa.service.member.Member
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MemberAdminService(
    private val memberAdminRepository: MemberAdminQueryRepository,
    private val memberStreakAdminRepository: MemberStreakLookupRepository,
    private val memberQuoteAdminRepository: MemberQuoteCompletionRepository,
    private val memberDeviceAdminRepository: MemberDeviceAdminQueryRepository,
) {

    @Transactional(readOnly = true)
    fun list(
        pageable: Pageable,
        keyword: String?,
        oauthProvider: Member.OAuthProvider?,
        withdrawalYn: String?,
    ): PageEnvelope<MemberListItem> {
        val effectivePageable = withDefaultSort(pageable)
        val page = memberAdminRepository.search(
            keyword = keyword?.takeIf { it.isNotBlank() },
            oauthProvider = oauthProvider,
            withdrawalYn = withdrawalYn?.takeIf { it.isNotBlank() },
            pageable = effectivePageable,
        )

        val currentStreaks = currentStreaksByMemberSeq(page.content.map { it.memberSeq })

        return PageEnvelope.from(page) { member ->
            MemberListItem.from(member, currentStreaks[member.memberSeq] ?: 0)
        }
    }

    @Transactional(readOnly = true)
    fun summary(): MemberSummaryResponse {
        val totalCount = memberAdminRepository.count()
        val withdrawnCount = memberAdminRepository.countByWithdrawalYn("Y")
        val adminCount = memberAdminRepository.countByAdminYn("Y")

        return MemberSummaryResponse(
            totalCount = totalCount,
            activeCount = totalCount - withdrawnCount,
            withdrawnCount = withdrawnCount,
            adminCount = adminCount,
        )
    }

    @Transactional(readOnly = true)
    fun detail(memberSeq: Long): MemberDetailResponse {
        val member = getMember(memberSeq)
        val streak = memberStreakAdminRepository.findByMember(member)
        val totalCompletedCount = memberQuoteAdminRepository.countByMemberAndCompleted(member, true).toInt()
        val devices = memberDeviceAdminRepository.findAllByMemberOrderByUpdatedAtDesc(member)
            .map { MemberDeviceResponse.from(it) }

        return MemberDetailResponse.from(
            member = member,
            currentStreak = streak?.currentStreakAsOf(LocalDate.now()) ?: 0,
            maxStreak = streak?.maxStreak ?: 0,
            totalCompletedCount = totalCompletedCount,
            devices = devices,
        )
    }

    @Transactional
    fun toggleAdminRole(memberSeq: Long, adminYn: String): MemberListItem {
        if (adminYn != "Y" && adminYn != "N") {
            throw BusinessException(ErrorCode.INVALID_VALUE, "adminYn 은 Y 또는 N 이어야 합니다: $adminYn")
        }

        val member = getMember(memberSeq)
        member.adminYn = adminYn

        val streak = memberStreakAdminRepository.findByMember(member)
        return MemberListItem.from(member, streak?.currentStreakAsOf(LocalDate.now()) ?: 0)
    }

    private fun getMember(memberSeq: Long): Member =
        memberAdminRepository.findById(memberSeq)
            .orElseThrow { BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 memberSeq: $memberSeq") }

    private fun currentStreaksByMemberSeq(memberSeqs: List<Long>): Map<Long, Int> {
        if (memberSeqs.isEmpty()) return emptyMap()
        val now = LocalDate.now()
        return memberStreakAdminRepository.findAllByMember_MemberSeqIn(memberSeqs)
            .associate { it.member.memberSeq to it.currentStreakAsOf(now) }
    }

    private fun withDefaultSort(pageable: Pageable): Pageable =
        if (pageable.sort.isSorted) {
            pageable
        } else {
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        }
}
