package com.fillsa.app.service.quote

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.app.common.redis.service.DailyQuoteCacheService
import com.fillsa.service.quote.DailyQuote
import com.fillsa.service.quote.DailyQuoteRepository
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