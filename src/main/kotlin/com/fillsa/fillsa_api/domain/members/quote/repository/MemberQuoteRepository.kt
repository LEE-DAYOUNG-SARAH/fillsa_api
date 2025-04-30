package com.fillsa.fillsa_api.domain.members.quote.repository

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import org.springframework.data.jpa.repository.JpaRepository

interface MemberQuoteRepository: JpaRepository<MemberQuote, Long> {
    fun findByMemberAndDailyQuoteDailyQuoteSeq(member: Member, dailyQuoteSeq: Long): MemberQuote?

    fun findByMemberAndMemberQuoteSeq(member: Member, memberQuoteSeq: Long): MemberQuote?
}