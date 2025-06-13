package store.fillsa.fillsa_api.domain.quote.repository

import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface DailyQuoteRepository: JpaRepository<DailyQuote, Long> {
    fun findByQuoteDate(quoteDate: LocalDate): DailyQuote?

    @Query("""
        select dq
        from DailyQuote dq
            join fetch dq.quote q
        where dq.quoteDate between :startDate and :endDate   
    """)
    fun findAllByQuoteDateBetween(startDate: LocalDate, endDate: LocalDate): List<DailyQuote>
}