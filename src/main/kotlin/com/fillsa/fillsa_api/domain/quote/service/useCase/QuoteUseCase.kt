package com.fillsa.fillsa_api.domain.quote.service.useCase

import com.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import org.springframework.core.io.Resource
import java.time.LocalDate

interface QuoteUseCase {
    /**
     *  일별 명언 조회
     */
    fun getDailyQuote(quoteDate: LocalDate): DailyQuoteResponse

    /**
     *  명언 이미지 조회
     */
    fun images(quoteSeq: Long): Resource
}