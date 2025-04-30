package com.fillsa.fillsa_api.domain.quote.service

import com.fillsa.fillsa_api.common.exception.NotFoundException
import com.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import com.fillsa.fillsa_api.domain.quote.service.useCase.DailyQuoteUseCase
import com.fillsa.fillsa_api.domain.quote.service.useCase.QuoteUseCase
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class QuoteService(
    private val dailyQuoteUseCase: DailyQuoteUseCase,
    @Value("\${fillsa.ko-author-url}")
    private val koAuthorUrl: String,
    @Value("\${fillsa.en-author-url}")
    private val enAuthorUrl: String,
): QuoteUseCase {

    @Transactional(readOnly = true)
    override fun getDailyQuote(quoteDate: LocalDate): DailyQuoteResponse {
        val dailyQuote = dailyQuoteUseCase.getDailyQuoteByQuoteDate(quoteDate)
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