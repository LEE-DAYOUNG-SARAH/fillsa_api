package store.fillsa.fillsa_api.common.redis.entity

import com.fasterxml.jackson.annotation.JsonFormat
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import store.fillsa.fillsa_api.domain.quote.entity.Quote
import java.time.LocalDate
import java.time.LocalDateTime

data class DailyQuoteCache(
    val id: String, // "{yyyy-MM-dd}" 형식

    @JsonFormat(pattern = "yyyy-MM-dd")
    val quoteDate: LocalDate,

    val dailyQuoteSeq: Long,
    val quoteSeq: Long,
    val korQuote: String?,
    val engQuote: String?,
    val korAuthor: String?,
    val engAuthor: String?,
    val category: String?,
    val quoteDayOfWeek: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun createId(date: LocalDate): String = date.toString()

        fun from(dailyQuote: DailyQuote): DailyQuoteCache {
            return DailyQuoteCache(
                id = createId(dailyQuote.quoteDate),
                quoteDate = dailyQuote.quoteDate,
                dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
                quoteSeq = dailyQuote.quote.quoteSeq,
                korQuote = dailyQuote.quote.korQuote,
                engQuote = dailyQuote.quote.engQuote,
                korAuthor = dailyQuote.quote.korAuthor,
                engAuthor = dailyQuote.quote.engAuthor,
                category = dailyQuote.quote.category,
                quoteDayOfWeek = dailyQuote.quoteDayOfWeek
            )
        }

        fun toDailyQuote(cache: DailyQuoteCache): DailyQuote {
            val quote = Quote(
                korQuote = cache.korQuote,
                engQuote = cache.engQuote,
                korAuthor = cache.korAuthor,
                engAuthor = cache.engAuthor,
                category = cache.category
            )

            return DailyQuote(
                quote = quote,
                dailyQuoteSeq = cache.dailyQuoteSeq,
                quoteDate = cache.quoteDate,
                quoteDayOfWeek = cache.quoteDayOfWeek
            )
        }
    }
}
