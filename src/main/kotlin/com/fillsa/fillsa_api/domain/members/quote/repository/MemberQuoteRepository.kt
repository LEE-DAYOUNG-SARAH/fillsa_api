package com.fillsa.fillsa_api.domain.members.quote.repository

import com.fillsa.fillsa_api.domain.members.member.entity.Member
import com.fillsa.fillsa_api.domain.members.quote.entity.MemberQuote
import com.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
    fun findByMemberAndQuoteDateBetween(
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
            and mq.likeYn in :likeYns
        order by dq.quoteDate desc
    """)
    fun findByMemberAndLikeYnIn(
        member: Member,
        likeYns: List<String>,
        pageable: Pageable
    ): Page<MemberQuote>
}