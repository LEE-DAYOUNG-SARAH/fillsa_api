package store.fillsa.fillsa_api.domain.quote.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.redis.service.DailyQuoteCacheService
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import store.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository
import java.time.LocalDate

@Service
class DailyQuoteService(
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val dailyQuoteCacheService: DailyQuoteCacheService
) {
    @Transactional(readOnly = true)
    fun getDailyQuoteByDailQuoteSeq(dailyQuoteSeq: Long): DailyQuote? {
        return dailyQuoteRepository.findByDailQuoteSeq(dailyQuoteSeq)
    }

    @Transactional(readOnly = true)
    fun getDailyQuoteByQuoteDate(quoteDate: LocalDate): DailyQuote? {
        val cache = dailyQuoteCacheService.getDailyQuote(quoteDate)
        if(cache != null) return cache

        val dailyQuote = dailyQuoteRepository.findByQuoteDate(quoteDate)
        if (dailyQuote != null) {
            dailyQuoteCacheService.cacheDailyQuote(dailyQuote)
        }

        return dailyQuote
    }

    @Transactional(readOnly = true)
    fun getDailyQuoteByQuotMonth(startDate: LocalDate, endDate: LocalDate): List<DailyQuote> {
        val cache = dailyQuoteCacheService.getMonthlyQuotes(startDate, endDate)
        if(cache.isNotEmpty()) return cache

        val dailyQuotes = dailyQuoteRepository.findAllByQuoteDateBetween(startDate, endDate)
        dailyQuoteCacheService.cacheMonthlyQuotes(dailyQuotes)

        return dailyQuotes
    }
}