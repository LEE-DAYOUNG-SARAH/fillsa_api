package com.fillsa.fillsa_api.domain.quote.service.useCase

import com.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import java.time.LocalDate

interface DailyQuoteUseCase {
    /**
     *  일별 명언 조회
     */
    fun getDailyQuoteByDailQuoteSeq(dailyQuoteSeq: Long): DailyQuote?

    fun getDailyQuoteByQuoteDate(quoteDate: LocalDate): DailyQuote?
}