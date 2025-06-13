package store.fillsa.fillsa_api.domain.quote.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.domain.quote.entity.DailyQuote
import store.fillsa.fillsa_api.domain.quote.repository.DailyQuoteRepository
import java.time.LocalDate
import java.time.YearMonth

@Service
class DailyQuoteService(
    private val dailyQuoteRepository: DailyQuoteRepository
) {
    @Transactional(readOnly = true)
    fun getDailyQuoteByDailQuoteSeq(dailyQuoteSeq: Long): DailyQuote? {
        return dailyQuoteRepository.findByIdOrNull(dailyQuoteSeq)
    }

    @Transactional(readOnly = true)
    fun getDailyQuoteByQuoteDate(quoteDate: LocalDate): DailyQuote? {
        return dailyQuoteRepository.findByQuoteDate(quoteDate)
    }

    @Transactional(readOnly = true)
    fun getDailyQuoteByQuotMonth(yearMonth: YearMonth): List<DailyQuote> {
        if(yearMonth.isAfter(YearMonth.now())) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "현재 월 이후는 조회할 수 없습니다.")
        }

        val startDate = yearMonth.atDay(1)
        val endDate = if (yearMonth == YearMonth.now())
            LocalDate.now() else yearMonth.atEndOfMonth()

        return dailyQuoteRepository.findAllByQuoteDateBetween(startDate, endDate)
    }
}