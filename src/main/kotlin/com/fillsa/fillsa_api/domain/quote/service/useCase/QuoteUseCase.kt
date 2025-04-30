package com.fillsa.fillsa_api.domain.quote.service.useCase

import com.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import java.time.LocalDate

interface QuoteUseCase {
    /**
     *  일별 명언 조회
     */
    fun getDailyQuote(quoteDate: LocalDate): DailyQuoteResponse
}