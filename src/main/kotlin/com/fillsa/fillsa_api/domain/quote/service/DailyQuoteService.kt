package com.fillsa.fillsa_api.domain.quote.service

import com.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import com.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository
import com.fillsa.fillsa_api.domain.quote.service.useCase.DailyQuoteUseCase
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DailyQuoteService(
    private val dailyQuoteRepository: DailyQuoteRepository
): DailyQuoteUseCase {
    override fun getDailyQuoteByDailQuoteSeq(dailyQuoteSeq: Long): DailyQuote? {
        return dailyQuoteRepository.findByIdOrNull(dailyQuoteSeq)
    }

    override fun getDailyQuoteByQuoteDate(quoteDate: LocalDate): DailyQuote? {
        return dailyQuoteRepository.findByQuoteDate(quoteDate)
    }
}