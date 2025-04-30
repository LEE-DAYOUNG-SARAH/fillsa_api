package com.fillsa.fillsa_api.domain.quote.service.useCase

import com.fillsa.fillsa_api.domain.quote.entity.DailyQuote

interface DailyQuoteUseCase {
    /**
     *  일별 명언 조회
     */
    fun getDailyQuote(dailyQuoteSeq: Long): DailyQuote?
}