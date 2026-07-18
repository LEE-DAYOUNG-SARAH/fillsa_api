package com.fillsa.admin.service.cache

import com.fasterxml.jackson.databind.ObjectMapper
import com.fillsa.service.quote.DailyQuote
import com.fillsa.service.quote.DailyQuoteRepository
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 일별 명언(daily-quotes) Redis 캐시 관리. bff:app 의 DailyQuoteCacheService 와
 * 동일한 ZSET 키(daily_quotes_sorted)를 다뤄, 앱이 조회하는 캐시를 그대로 미리보기/갱신/삭제한다.
 */
@Service
class CacheAdminService(
    private val redisTemplate: StringRedisTemplate,
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private const val DAILY_QUOTE_CACHE_KEY = "daily_quotes_sorted"
    }

    @Transactional(readOnly = true)
    fun preview(): CachedDailyQuotesResponse {
        val records = readAll()
        return CachedDailyQuotesResponse(
            count = records.size,
            entries = records.map { CachedDailyQuoteEntry.from(it) },
        )
    }

    @Transactional(readOnly = true)
    fun refresh(request: CacheRefreshRequest): CacheRefreshResponse {
        val refreshedCount = request.date?.let { refreshSingleDate(it) } ?: refreshAll()
        return CacheRefreshResponse(refreshedCount = refreshedCount)
    }

    fun evict() {
        redisTemplate.delete(DAILY_QUOTE_CACHE_KEY)
    }

    private fun refreshSingleDate(date: LocalDate): Int {
        val dailyQuote = dailyQuoteRepository.findByQuoteDate(date)
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "해당 날짜의 일별 명언 배정이 없습니다.")

        val score = dateToScore(date)
        redisTemplate.opsForZSet().removeRangeByScore(DAILY_QUOTE_CACHE_KEY, score, score)
        cache(dailyQuote)
        return 1
    }

    private fun refreshAll(): Int {
        redisTemplate.delete(DAILY_QUOTE_CACHE_KEY)
        val dailyQuotes = dailyQuoteRepository.findAllOrderByQuoteDateAsc()
        dailyQuotes.forEach { cache(it) }
        return dailyQuotes.size
    }

    private fun cache(dailyQuote: DailyQuote) {
        val record = DailyQuoteCacheRecord.from(dailyQuote)
        val score = dateToScore(dailyQuote.quoteDate)
        val json = objectMapper.writeValueAsString(record)
        redisTemplate.opsForZSet().add(DAILY_QUOTE_CACHE_KEY, json, score)
    }

    private fun readAll(): List<DailyQuoteCacheRecord> {
        val jsonEntries = redisTemplate.opsForZSet().range(DAILY_QUOTE_CACHE_KEY, 0, -1) ?: emptySet()
        return jsonEntries.map { objectMapper.readValue(it, DailyQuoteCacheRecord::class.java) }
    }

    private fun dateToScore(date: LocalDate): Double = date.toEpochDay().toDouble()
}
