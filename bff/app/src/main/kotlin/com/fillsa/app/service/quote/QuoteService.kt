package com.fillsa.app.service.quote

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.fillsa.util.exception.ErrorCode
import com.fillsa.util.exception.BusinessException
import com.fillsa.app.api.quote.DailyQuoteResponse
import com.fillsa.app.api.quote.MonthlyQuoteResponse
import java.time.LocalDate
import java.time.YearMonth

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
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 quoteDate: $quoteDate")

        return DailyQuoteResponse.from(koAuthorUrl, enAuthorUrl, dailyQuote)
    }

    fun monthlyQuotes(yearMonth: YearMonth): List<MonthlyQuoteResponse> {
        if(yearMonth.isAfter(YearMonth.now())) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "현재 월 이후는 조회할 수 없습니다.")
        }

        val startDate = yearMonth.atDay(1)
        val endDate = if (yearMonth == YearMonth.now())
            LocalDate.now() else yearMonth.atEndOfMonth()

        return dailyQuoteService.getDailyQuoteByQuotMonth(startDate, endDate)
            .map { MonthlyQuoteResponse.from(it) }
    }
}