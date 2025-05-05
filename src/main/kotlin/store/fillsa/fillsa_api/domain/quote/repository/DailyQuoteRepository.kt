package store.fillsa.fillsa_api.domain.quote.repository

import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface DailyQuoteRepository: JpaRepository<DailyQuote, Long> {
    fun findByQuoteDate(quoteDate: LocalDate): DailyQuote?
}