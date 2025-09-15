package store.fillsa.fillsa_api.common.redis.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.redis.dto.DailyQuoteCacheRequest
import store.fillsa.fillsa_api.common.redis.dto.DailyQuoteCacheResponse
import store.fillsa.fillsa_api.common.redis.entity.DailyQuoteCache
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import store.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository
import java.time.LocalDate

@Service
class DailyQuoteCacheService(
    private val redisTemplate: StringRedisTemplate,
    private val dailyQuoteRepository: DailyQuoteRepository,
    private val objectMapper: ObjectMapper
) {
    private val SORTED_SET_KEY = "daily_quotes_sorted"
    
    private fun dateToScore(date: LocalDate): Double {
        return date.toEpochDay().toDouble()
    }
    
    private fun scoreToDate(score: Double): LocalDate {
        return LocalDate.ofEpochDay(score.toLong())
    }

    fun getDailyQuote(date: LocalDate): DailyQuote? {
        val score = dateToScore(date)
        val jsonData = redisTemplate.opsForZSet().rangeByScore(SORTED_SET_KEY, score, score)?.firstOrNull()
        return jsonData?.let { 
            val cache = objectMapper.readValue(it, DailyQuoteCache::class.java)
            DailyQuoteCache.toDailyQuote(cache)
        }
    }

    fun cacheDailyQuote(quote: DailyQuote): DailyQuoteCache {
        val cacheEntity = DailyQuoteCache.from(quote)
        val score = dateToScore(quote.quoteDate)
        val jsonData = objectMapper.writeValueAsString(cacheEntity)
        
        redisTemplate.opsForZSet().add(SORTED_SET_KEY, jsonData, score)
        return cacheEntity
    }

    fun getMonthlyQuotes(startDate: LocalDate, endDate: LocalDate): List<DailyQuote> {
        val startScore = dateToScore(startDate)
        val endScore = dateToScore(endDate)
        
        val jsonDataList = redisTemplate.opsForZSet().rangeByScore(SORTED_SET_KEY, startScore, endScore)
        return jsonDataList?.map { jsonData ->
            val cache = objectMapper.readValue(jsonData, DailyQuoteCache::class.java)
            DailyQuoteCache.toDailyQuote(cache)
        } ?: emptyList()
    }

    fun cacheMonthlyQuotes(quotes: List<DailyQuote>) {
        quotes.forEach { quote ->
            cacheDailyQuote(quote)
        }
    }

    fun refreshQuote(date: LocalDate): DailyQuoteCacheResponse {
        val dailyQuote = dailyQuoteRepository.findByQuoteDate(date)
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "해당 날짜의 DailyQuote 데이터가 없습니다.")

        // 기존 캐시 삭제
        val score = dateToScore(date)
        redisTemplate.opsForZSet().removeRangeByScore(SORTED_SET_KEY, score, score)

        // 새로운 캐시 추가
        val cache = cacheDailyQuote(dailyQuote)
        return DailyQuoteCacheResponse.from(cache)
    }

    fun reloadQuotes(request: DailyQuoteCacheRequest): List<DailyQuoteCacheResponse> {
        // Delete existing caches in the date range
        deleteQuotesByDateRange(request.startDate, request.endDate)
        
        val quotes = dailyQuoteRepository.findAllByQuoteDateBetween(request.startDate, request.endDate)
        
        val cacheEntities = quotes.map { quote ->
            cacheDailyQuote(quote)
        }

        return cacheEntities.map { DailyQuoteCacheResponse.from(it) }
    }

    fun getQuotes(request: DailyQuoteCacheRequest): List<DailyQuoteCacheResponse> {
        val startScore = dateToScore(request.startDate)
        val endScore = dateToScore(request.endDate)
        
        val jsonDataList = redisTemplate.opsForZSet().rangeByScore(SORTED_SET_KEY, startScore, endScore)
        return jsonDataList?.map { jsonData ->
            val cache = objectMapper.readValue(jsonData, DailyQuoteCache::class.java)
            DailyQuoteCacheResponse.from(cache)
        } ?: emptyList()
    }

    fun deleteQuotes(request: DailyQuoteCacheRequest) {
        deleteQuotesByDateRange(request.startDate, request.endDate)
    }
    
    private fun deleteQuotesByDateRange(startDate: LocalDate, endDate: LocalDate) {
        val startScore = dateToScore(startDate)
        val endScore = dateToScore(endDate)
        redisTemplate.opsForZSet().removeRangeByScore(SORTED_SET_KEY, startScore, endScore)
    }
}