package store.fillsa.fillsa_api.domain.members.quote.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.dto.MemberStreakResponse
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberStreak
import store.fillsa.fillsa_api.domain.members.quote.repository.MemberStreakRepository
import java.time.LocalDate

@Service
class MemberStreakService(
    private val memberStreakRepository: MemberStreakRepository
) {
    @Transactional(readOnly = true)
    fun getStreak(member: Member): MemberStreakResponse {
        val memberStreak = findStreak(member)
        return MemberStreakResponse.from(memberStreak, LocalDate.now())
    }

    @Transactional
    fun recordTodayCompletion(quoteDate: LocalDate, member: Member) {
        if (quoteDate != LocalDate.now()) return

        val memberStreak = findStreak(member)
        memberStreak.recordTodayCompletion(quoteDate)
    }

    private fun findStreak(member: Member): MemberStreak {
        return memberStreakRepository.findByMember(member)
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 memberStreak")
    }

    @Transactional
    fun create(member: Member) {
        if(memberStreakRepository.existsByMember(member)) return

        memberStreakRepository.save(
            MemberStreak(member = member)
        )
    }
}