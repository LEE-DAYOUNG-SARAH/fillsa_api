package com.fillsa.service.member

import org.springframework.data.jpa.repository.JpaRepository
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberStreak

interface MemberStreakRepository: JpaRepository<MemberStreak, Long> {
    fun findByMember(member: Member): MemberStreak?

    fun existsByMember(member: Member): Boolean
}