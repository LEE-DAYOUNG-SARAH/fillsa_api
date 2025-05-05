package store.fillsa.fillsa_api.domain.quote.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.NotFoundException
import store.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import java.time.LocalDate

@Service
class QuoteService(
    private val dailyQuoteService: DailyQuoteService,
    @Value("\${fillsa.ko-author-url}")
    private val koAuthorUrl: String,
    @Value("\${fillsa.en-author-url}")
    private val enAuthorUrl: String,
) {

    @Transactional(readOnly = true)
    fun getDailyQuote(quoteDate: LocalDate): DailyQuoteResponse {
        val dailyQuote = dailyQuoteService.getDailyQuoteByQuoteDate(quoteDate)
            ?: throw NotFoundException("존재하지 않는 quoteDate: $quoteDate")

        return DailyQuoteResponse(
            dailyQuoteSeq = dailyQuote.dailyQuoteSeq,
            korQuote = dailyQuote.quote.korQuote,
            engQuote = dailyQuote.quote.engQuote,
            korAuthor = dailyQuote.quote.korAuthor,
            engAuthor = dailyQuote.quote.engAuthor,
            authorUrl = dailyQuote.quote.korAuthor?.let { "${koAuthorUrl}$it" }
                ?: "${enAuthorUrl}${dailyQuote.quote.engAuthor}"
        )
    }
}