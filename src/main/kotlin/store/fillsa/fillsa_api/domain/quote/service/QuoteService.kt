package store.fillsa.fillsa_api.domain.quote.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.domain.quote.dto.DailyQuoteResponse
import store.fillsa.fillsa_api.domain.quote.dto.MonthlyQuoteResponse
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