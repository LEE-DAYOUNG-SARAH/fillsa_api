package com.fillsa.admin.service.memberquote

import com.fillsa.service.member.MemberQuote
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface MemberQuoteAdminQueryRepository : JpaRepository<MemberQuote, Long> {

    /**
     * 어드민 필사 기록 목록. keyword 는 회원 닉네임·명언(kor/eng) 부분일치,
     * quoteDate·completed 는 정확일치. 세 조건 모두 null 이면 무시된다.
     */
    @Query(
        """
        select mq
        from MemberQuote mq
            join fetch mq.member m
            join fetch mq.dailyQuote dq
            join fetch dq.quote q
        where (:keyword is null
                or lower(m.nickname) like lower(concat('%', :keyword, '%'))
                or lower(q.korQuote) like lower(concat('%', :keyword, '%'))
                or lower(q.engQuote) like lower(concat('%', :keyword, '%')))
            and (:quoteDate is null or dq.quoteDate = :quoteDate)
            and (:completed is null or mq.completed = :completed)
        """,
        countQuery = """
        select count(mq)
        from MemberQuote mq
            join mq.member m
            join mq.dailyQuote dq
            join dq.quote q
        where (:keyword is null
                or lower(m.nickname) like lower(concat('%', :keyword, '%'))
                or lower(q.korQuote) like lower(concat('%', :keyword, '%'))
                or lower(q.engQuote) like lower(concat('%', :keyword, '%')))
            and (:quoteDate is null or dq.quoteDate = :quoteDate)
            and (:completed is null or mq.completed = :completed)
        """,
    )
    fun search(
        @Param("keyword") keyword: String?,
        @Param("quoteDate") quoteDate: LocalDate?,
        @Param("completed") completed: Boolean?,
        pageable: Pageable,
    ): Page<MemberQuote>

    @Query(
        """
        select mq
        from MemberQuote mq
            join fetch mq.member m
            join fetch mq.dailyQuote dq
            join fetch dq.quote q
        where mq.memberQuoteSeq = :memberQuoteSeq
        """,
    )
    fun findDetailByMemberQuoteSeq(@Param("memberQuoteSeq") memberQuoteSeq: Long): MemberQuote?
}
