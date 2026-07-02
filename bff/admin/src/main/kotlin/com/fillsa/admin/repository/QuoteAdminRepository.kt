package com.fillsa.admin.repository

import com.fillsa.service.quote.Quote
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface QuoteAdminRepository : JpaRepository<Quote, Long> {

    /**
     * 어드민 명언 목록. 삭제(delYn='Y') 제외. keyword 는 명언(kor/eng)·작가(kor/eng) 부분일치,
     * category 는 정확일치. 두 조건 모두 null 이면 무시된다.
     */
    @Query(
        """
        select q
        from Quote q
        where q.delYn = 'N'
            and (:keyword is null
                or lower(q.korQuote) like lower(concat('%', :keyword, '%'))
                or lower(q.engQuote) like lower(concat('%', :keyword, '%'))
                or lower(q.korAuthor) like lower(concat('%', :keyword, '%'))
                or lower(q.engAuthor) like lower(concat('%', :keyword, '%')))
            and (:category is null or q.category = :category)
        """,
    )
    fun search(
        @Param("keyword") keyword: String?,
        @Param("category") category: String?,
        pageable: Pageable,
    ): Page<Quote>

    fun findByQuoteSeqAndDelYn(quoteSeq: Long, delYn: String): Quote?
}
