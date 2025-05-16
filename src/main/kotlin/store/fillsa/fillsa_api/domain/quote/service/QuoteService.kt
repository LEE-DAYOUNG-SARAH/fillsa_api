package store.fillsa.fillsa_api.domain.quote.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.exception.BusinessException
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
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 quoteDate: $quoteDate")

        return DailyQuoteResponse.from(koAuthorUrl, enAuthorUrl, dailyQuote)
    }
}