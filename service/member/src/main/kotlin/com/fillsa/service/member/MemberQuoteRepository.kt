package com.fillsa.service.member

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import com.fillsa.service.member.Member
import com.fillsa.service.member.MemberQuote
import com.fillsa.service.quote.DailyQuote
import java.time.LocalDate

interface MemberQuoteRepository: JpaRepository<MemberQuote, Long> {
    fun findByMemberAndDailyQuoteDailyQuoteSeq(member: Member, dailyQuoteSeq: Long): MemberQuote?

    fun findByMemberAndMemberQuoteSeq(member: Member, memberQuoteSeq: Long): MemberQuote?

    fun findByMemberAndDailyQuote(member: Member, dailyQuote: DailyQuote): MemberQuote?

    fun findAllByMemberAndDailyQuoteIn(member: Member, dailyQuotes: List<DailyQuote>): List<MemberQuote>

    @Query("""
        select mq
        from MemberQuote mq
            join fetch mq.member m
            join fetch mq.dailyQuote dq
            join fetch dq.quote q
        where m = :member
            and dq.quoteDate between :beginQuoteDate and :endQuoteDate
    """)
    fun findAllByMemberAndQuoteDateBetween(
        member: Member,
        beginQuoteDate: LocalDate,
        endQuoteDate: LocalDate
    ): List<MemberQuote>

    @Query("""
        select mq
        from MemberQuote mq
            join fetch mq.member m
            join fetch mq.dailyQuote dq
            join fetch dq.quote q
        where m = :member
            and dq.quoteDate between :startDate and :endDate
        order by dq.quoteDate desc
    """)
    fun findAllByMemberAndCreatedAtBetween(member: Member, startDate: LocalDate, endDate: LocalDate): List<MemberQuote>

    @Query("""
        select mq
        from MemberQuote mq
            join fetch mq.dailyQuote dq
        where mq.memberQuoteSeq = :memberQuoteSeq
    """)
    fun findByMemberQuoteSeq(memberQuoteSeq: Long): MemberQuote?
}