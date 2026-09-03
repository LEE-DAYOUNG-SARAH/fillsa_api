package com.fillsa.app.api.members.quote

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

/**
 * 홈 상단 롤링 7일 응답.
 *
 * 달력상의 주가 아니라 `endDate` 를 마지막 칸으로 하는 7일 창이다 (endDate-6 ~ endDate).
 * 클라이언트는 응답의 endDate 에서 ±7 하여 창을 이동한다.
 */
data class WeeklyQuoteResponse(
    @Schema(description = "창의 시작 날짜(endDate - 6)", required = true, example = "2026-08-28")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    val startDate: LocalDate,

    @Schema(description = "창의 마지막 날짜", required = true, example = "2026-09-03")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    val endDate: LocalDate,

    @Schema(description = "7일치 명언 카드 (오름차순)", required = true)
    val days: List<DayQuoteData>
) {
    companion object {
        const val WINDOW_SIZE = 7L

        fun of(startDate: LocalDate, endDate: LocalDate, days: List<DayQuoteData>) =
            WeeklyQuoteResponse(startDate, endDate, days)
    }
}
