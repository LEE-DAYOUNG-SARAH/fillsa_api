package store.fillsa.fillsa_api.domain.quote.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import store.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository
import java.time.LocalDate

@Service
class DailyQuoteService(
    private val dailyQuoteRepository: DailyQuoteRepository
) {
    @Transactional(readOnly = true)
    fun getDailyQuoteByDailQuoteSeq(dailyQuoteSeq: Long): DailyQuote? {
        return dailyQuoteRepository.findByIdOrNull(dailyQuoteSeq)
    }

    @Transactional(readOnly = true)
    fun getDailyQuoteByQuoteDate(quoteDate: LocalDate): DailyQuote? {
        return dailyQuoteRepository.findByQuoteDate(quoteDate)
    }
}