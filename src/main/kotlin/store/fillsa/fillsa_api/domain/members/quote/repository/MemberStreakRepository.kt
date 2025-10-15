package store.fillsa.fillsa_api.domain.members.quote.repository

import org.springframework.data.jpa.repository.JpaRepository
import store.fillsa.fillsa_api.domain.members.member.entity.Member
import store.fillsa.fillsa_api.domain.members.quote.entity.MemberStreak

interface MemberStreakRepository: JpaRepository<MemberStreak, Long> {
    fun findByMember(member: Member): MemberStreak?

    fun existsByMember(member: Member): Boolean
}