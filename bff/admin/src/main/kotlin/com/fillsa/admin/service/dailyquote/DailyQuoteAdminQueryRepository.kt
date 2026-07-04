package com.fillsa.admin.service.dailyquote

import com.fillsa.service.quote.DailyQuote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface DailyQuoteAdminQueryRepository : JpaRepository<DailyQuote, Long> {

    /** 특정 quote 를 참조하는 daily_quotes 배정 건수. 삭제 가능 여부(=0) 판단에 사용. */
    fun countByQuoteQuoteSeq(quoteSeq: Long): Long

    /** quoteSeq 목록별 배정 건수(assignedCount 배치 조회). */
    @Query(
        """
        select dq.quote.quoteSeq as quoteSeq, count(dq) as assignedCount
        from DailyQuote dq
        where dq.quote.quoteSeq in :quoteSeqs
        group by dq.quote.quoteSeq
        """,
    )
    fun countAssignedByQuoteSeqs(@Param("quoteSeqs") quoteSeqs: List<Long>): List<AssignedCountProjection>

    /**
     * 월 범위 배정 조회(어드민). 삭제 여부와 무관하게 배정 이력을 모두 조회한다.
     */
    @Query(
        """
        select dq
        from DailyQuote dq
            join fetch dq.quote q
        where dq.quoteDate between :startDate and :endDate
        order by dq.quoteDate asc
        """,
    )
    fun findAllByQuoteDateBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
    ): List<DailyQuote>

    @Query(
        """
        select dq
        from DailyQuote dq
            join fetch dq.quote q
        where dq.quoteDate = :quoteDate
        """,
    )
    fun findByQuoteDate(@Param("quoteDate") quoteDate: LocalDate): DailyQuote?

    /** 미사용 우선 자동 배정: 삭제되지 않은 명언별 배정 횟수(0 포함)를 오름차순으로 반환. */
    @Query(
        """
        select q.quoteSeq as quoteSeq, count(dq) as assignedCount
        from Quote q
            left join DailyQuote dq on dq.quote = q
        where q.delYn = 'N'
        group by q.quoteSeq
        order by count(dq) asc, q.quoteSeq asc
        """,
    )
    fun findAssignableQuoteStats(): List<AssignedCountProjection>
}

interface AssignedCountProjection {
    val quoteSeq: Long
    val assignedCount: Long
}
