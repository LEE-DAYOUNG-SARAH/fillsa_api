package com.fillsa.admin.service.quote

import com.fillsa.service.quote.Quote
import java.time.LocalDateTime

data class QuoteSaveRequest(
    val korQuote: String? = null,
    val engQuote: String? = null,
    val korAuthor: String? = null,
    val engAuthor: String? = null,
    val category: String? = null,
    val questionKo: String? = null,
    val questionEn: String? = null,
)

data class QuoteResponse(
    val quoteSeq: Long,
    val korQuote: String?,
    val engQuote: String?,
    val korAuthor: String?,
    val engAuthor: String?,
    val category: String?,
    val questionKo: String?,
    val questionEn: String?,
    val assignedCount: Int,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(quote: Quote, assignedCount: Int) = QuoteResponse(
            quoteSeq = quote.quoteSeq,
            korQuote = quote.korQuote,
            engQuote = quote.engQuote,
            korAuthor = quote.korAuthor,
            engAuthor = quote.engAuthor,
            category = quote.category,
            questionKo = quote.questionKo,
            questionEn = quote.questionEn,
            assignedCount = assignedCount,
            createdAt = quote.createdAt,
        )
    }
}
