package com.fillsa.admin.service.dailyquote

import com.fillsa.admin.service.quote.QuoteAdminQueryRepository
import com.fillsa.admin.service.quote.QuoteResponse
import com.fillsa.service.quote.DailyQuote
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException

@Service
class DailyQuoteAdminService(
    private val dailyQuoteAdminRepository: DailyQuoteAdminQueryRepository,
    private val quoteAdminRepository: QuoteAdminQueryRepository,
) {

    @Transactional(readOnly = true)
    fun monthView(yearMonth: String): DailyQuoteMonthResponse {
        val ym = parseYearMonth(yearMonth)
        val startDate = ym.atDay(1)
        val endDate = ym.atEndOfMonth()

        val assignmentsByDate = dailyQuoteAdminRepository.findAllByQuoteDateBetween(startDate, endDate)
            .associateBy { it.quoteDate }

        val assignedCounts = assignedCounts(assignmentsByDate.values.map { it.quote.quoteSeq })

        val days = (1..ym.lengthOfMonth()).map { day ->
            val date = ym.atDay(day)
            toDayResponse(date, assignmentsByDate[date], assignedCounts)
        }

        val assignedCount = days.count { it.assigned }
        return DailyQuoteMonthResponse(
            yearMonth = yearMonth,
            assignedCount = assignedCount,
            unassignedCount = days.size - assignedCount,
            days = days,
        )
    }

    @Transactional
    fun assign(date: LocalDate, request: AssignDailyQuoteRequest): DailyQuoteDayResponse {
        val quote = quoteAdminRepository.findByQuoteSeqAndDelYn(request.quoteSeq, "N")
            ?: throw BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 quoteSeq: ${request.quoteSeq}")

        val dayOfWeek = KoreanDayOfWeek.of(date)
        val existing = dailyQuoteAdminRepository.findByQuoteDate(date)

        val saved = if (existing != null) {
            existing.quote = quote
            existing.quoteDayOfWeek = dayOfWeek
            existing
        } else {
            dailyQuoteAdminRepository.save(
                DailyQuote(quote = quote, quoteDate = date, quoteDayOfWeek = dayOfWeek),
            )
        }

        val assignedCount = dailyQuoteAdminRepository.countByQuoteQuoteSeq(quote.quoteSeq).toInt()
        return DailyQuoteDayResponse(
            date = date,
            dayOfWeek = dayOfWeek,
            assigned = true,
            dailyQuoteSeq = saved.dailyQuoteSeq,
            quote = QuoteResponse.from(quote, assignedCount),
        )
    }

    private fun toDayResponse(
        date: LocalDate,
        assignment: DailyQuote?,
        assignedCounts: Map<Long, Long>,
    ): DailyQuoteDayResponse {
        if (assignment == null) {
            return DailyQuoteDayResponse(date = date, dayOfWeek = KoreanDayOfWeek.of(date), assigned = false)
        }
        val quote = assignment.quote
        return DailyQuoteDayResponse(
            date = date,
            dayOfWeek = assignment.quoteDayOfWeek,
            assigned = true,
            dailyQuoteSeq = assignment.dailyQuoteSeq,
            quote = QuoteResponse.from(quote, assignedCounts[quote.quoteSeq]?.toInt() ?: 0),
        )
    }

    private fun assignedCounts(quoteSeqs: List<Long>): Map<Long, Long> {
        if (quoteSeqs.isEmpty()) return emptyMap()
        return dailyQuoteAdminRepository.countAssignedByQuoteSeqs(quoteSeqs.distinct())
            .associate { it.quoteSeq to it.assignedCount }
    }

    private fun parseYearMonth(yearMonth: String): YearMonth =
        try {
            YearMonth.parse(yearMonth)
        } catch (e: DateTimeParseException) {
            throw BusinessException(ErrorCode.INVALID_REQUEST, "잘못된 yearMonth 형식: $yearMonth")
        }
}
