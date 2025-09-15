package store.fillsa.fillsa_api.common.redis.dto

import store.fillsa.fillsa_api.common.redis.entity.DailyQuoteCache
import java.time.LocalDate
import java.time.LocalDateTime

data class DailyQuoteCacheRequest(
    val startDate: LocalDate = LocalDate.of(2025, 6, 10),
    val endDate: LocalDate
)

data class DailyQuoteCacheResponse(
    val id: String,
    val quoteDate: LocalDate,
    val dailyQuoteSeq: Long,
    val quoteSeq: Long,
    val korQuote: String?,
    val engQuote: String?,
    val korAuthor: String?,
    val engAuthor: String?,
    val category: String?,
    val quoteDayOfWeek: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(cache: DailyQuoteCache) = DailyQuoteCacheResponse(
            id = cache.id,
            quoteDate = cache.quoteDate,
            dailyQuoteSeq = cache.dailyQuoteSeq,
            quoteSeq = cache.quoteSeq,
            korQuote = cache.korQuote,
            engQuote = cache.engQuote,
            korAuthor = cache.korAuthor,
            engAuthor = cache.engAuthor,
            category = cache.category,
            quoteDayOfWeek = cache.quoteDayOfWeek,
            createdAt = cache.createdAt
        )
    }
}