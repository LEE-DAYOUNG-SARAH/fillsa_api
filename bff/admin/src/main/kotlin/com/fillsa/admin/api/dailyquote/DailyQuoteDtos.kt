package com.fillsa.admin.api.dailyquote

import com.fillsa.admin.api.quote.QuoteResponse
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * quoteDayOfWeek 컬럼 값 규칙: 한글 단일 문자 요일(월/화/수/목/금/토/일).
 * (기존 데이터/픽스처가 "월" 등 char(1) 한글 요일을 사용하는 관례를 따른다.)
 */
object KoreanDayOfWeek {
    private val MAP = mapOf(
        DayOfWeek.MONDAY to "월",
        DayOfWeek.TUESDAY to "화",
        DayOfWeek.WEDNESDAY to "수",
        DayOfWeek.THURSDAY to "목",
        DayOfWeek.FRIDAY to "금",
        DayOfWeek.SATURDAY to "토",
        DayOfWeek.SUNDAY to "일",
    )

    fun of(date: LocalDate): String = MAP.getValue(date.dayOfWeek)
}

data class DailyQuoteDayResponse(
    val date: LocalDate,
    val dayOfWeek: String,
    val assigned: Boolean,
    val dailyQuoteSeq: Long? = null,
    val quote: QuoteResponse? = null,
)

data class DailyQuoteMonthResponse(
    val yearMonth: String,
    val assignedCount: Int,
    val unassignedCount: Int,
    val days: List<DailyQuoteDayResponse>,
)

data class AssignDailyQuoteRequest(
    val quoteSeq: Long,
)

data class AutoAssignRequest(
    val yearMonth: String,
)

data class AutoAssignResponse(
    val assignedCount: Int,
    val assignedDates: List<LocalDate>,
)
