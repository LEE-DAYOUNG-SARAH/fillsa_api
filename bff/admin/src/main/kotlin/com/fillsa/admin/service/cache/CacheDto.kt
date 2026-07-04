package com.fillsa.admin.service.cache

import com.fillsa.service.quote.DailyQuote
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Redis 저장 포맷(daily_quotes_sorted ZSET 멤버) — bff:app 의 DailyQuoteCache 엔티티와
 * 필드가 동일해야 두 BFF 가 같은 캐시 데이터를 상호 호환해서 읽고 쓸 수 있다.
 */
data class DailyQuoteCacheRecord(
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
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun from(dailyQuote: DailyQuote) = DailyQuoteCacheRecord(
            id = dailyQuote.quoteDate.toString(),
            quoteDate = dailyQuote.quoteDate,
            dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
            quoteSeq = dailyQuote.quote.quoteSeq,
            korQuote = dailyQuote.quote.korQuote,
            engQuote = dailyQuote.quote.engQuote,
            korAuthor = dailyQuote.quote.korAuthor,
            engAuthor = dailyQuote.quote.engAuthor,
            category = dailyQuote.quote.category,
            quoteDayOfWeek = dailyQuote.quoteDayOfWeek,
        )
    }
}

data class CachedDailyQuoteEntry(
    val date: LocalDate,
    val korQuote: String?,
    val korAuthor: String?,
) {
    companion object {
        fun from(record: DailyQuoteCacheRecord) = CachedDailyQuoteEntry(
            date = record.quoteDate,
            korQuote = record.korQuote,
            korAuthor = record.korAuthor,
        )
    }
}

data class CachedDailyQuotesResponse(
    val count: Int,
    val entries: List<CachedDailyQuoteEntry>,
)

data class CacheRefreshRequest(
    val date: LocalDate? = null,
)

data class CacheRefreshResponse(
    val refreshedCount: Int,
)
