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

    /**
     * 기간 내 일별 명언을 DB 에서 직접 조회한다 (캐시 미사용).
     *
     * getDailyQuoteByQuotMonth 는 캐시에 하나라도 있으면 그것만 반환하므로,
     * 기간 일부만 캐시된 상태에서는 빠진 날짜를 DB 에서 채우지 않는다.
     * 홈 주간 조회는 7일이 모두 정확해야 하므로 캐시를 거치지 않는다.
     */
    @Transactional(readOnly = true)
    fun getDailyQuotesInRange(startDate: LocalDate, endDate: LocalDate): List<DailyQuote> {
        return dailyQuoteRepository.findAllByQuoteDateBetween(startDate, endDate)
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